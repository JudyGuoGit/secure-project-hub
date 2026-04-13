# PKI Testing Guide - Step by Step Commands

This guide provides all the commands you need to test the PKI (Public Key Infrastructure) / mTLS authentication in the Secure Project Hub.

---

## Prerequisites

1. **Java 21** installed
2. **Maven** installed
3. **curl** installed (macOS comes with it)
4. **openssl** installed (macOS comes with it)
5. **PostgreSQL** running in Docker (optional, for full testing)

Check versions:
```bash
java -version
mvn --version
curl --version
openssl version
```

---

## Phase 1: Build and Start the Application

### Step 1.1: Navigate to Project Directory
```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
```

### Step 1.2: Ensure Certificates and Keystores Exist
```bash
# Check if certificates are already generated
ls -la certs/
```

**Expected files:**
- `ca-cert.pem` (CA certificate - public)
- `ca-key.pem` (CA private key - secret)
- `server-cert.pem` (Server certificate)
- `server-key.pem` (Server private key)
- `client-cert.pem` (Client certificate - needed for mTLS)
- `client-key.pem` (Client private key)
- `keystore.p12` (Server keystore)
- `truststore.p12` (Server truststore)

### Step 1.3: If Certificates Don't Exist, Generate Them
```bash
# Run the certificate generation script
bash scripts/generate-certificates.sh
```

### Step 1.4: Copy Keystores to Resources
```bash
# Copy to main resources
cp certs/keystore.p12 src/main/resources/
cp certs/truststore.p12 src/main/resources/
```

### Step 1.5: Build the Application
```bash
# Build with Maven
mvn clean package -DskipTests
```

### Step 1.6: Start PostgreSQL (if using Docker)
```bash
# Start Docker container
docker-compose up -d postgres
```

**Wait for PostgreSQL to be ready:**
```bash
# Check PostgreSQL is running
docker-compose ps postgres
```

### Step 1.7: Start the Spring Boot Application
```bash
# Option A: Using Spring Boot Maven plugin (simplest)
mvn spring-boot:run

# OR

# Option B: Run the JAR directly
java -jar target/secure-project-hub-0.0.1-SNAPSHOT.jar

# The app should start on:
# - HTTP:  http://localhost:8080
# - HTTPS: https://localhost:8443
```

**Wait for startup messages:**
- Look for: `"Tomcat started on port(s): 8080 (http) and 8443 (https)"`
- Wait another 10-15 seconds for full initialization

---

## Phase 2: Verify Application is Running

### Step 2.1: Check Health Endpoint (HTTP - No Auth)
```bash
# This should return 200 OK without authentication
curl -s http://localhost:8080/actuator/health | jq .
```

**Expected output:**
```json
{
  "status": "UP"
}
```

### Step 2.2: Check Application is Ready for SSL/TLS
```bash
# Try HTTPS without client certificate (should fail or warn)
curl -k https://localhost:8443/actuator/health 2>&1 | head -20
```

**Note:** The `-k` flag ignores certificate verification for now.

---

## Phase 3: Test OAuth2 First (Baseline)

### Step 3.1: Get OAuth2 Token
```bash
# Request a JWT token using OAuth2
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token // .access_token')

# Check the token was obtained
echo "Token: $TOKEN"
```

**Expected:** You should see a long JWT token (similar to: `eyJhbGc...`)

### Step 3.2: Decode the Token (Optional)
```bash
# Decode the JWT payload (middle part)
echo $TOKEN | cut -d'.' -f2 | base64 -d | jq .
```

**Expected:** JSON showing user info, roles, and expiration

### Step 3.3: Use Token to Access Protected Resource
```bash
# Use the token to access a resource
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users | jq .
```

**Expected:** List of users (OAuth2 authentication working)

---

## Phase 4: Test PKI/mTLS (The Main Event)

### Step 4.1: Verify Client Certificate Files Exist
```bash
# Check client certificate files
ls -lh certs/client-cert.pem certs/client-key.pem certs/ca-cert.pem
```

**Expected:** All three files should exist

### Step 4.2: Inspect the Client Certificate
```bash
# View certificate details
openssl x509 -in certs/client-cert.pem -text -noout | head -30
```

**Expected output should show:**
- Subject: CN (Common Name)
- Issuer: O (Organization)
- Validity dates
- Public Key

### Step 4.3: Test 1 - PKI Health Check (Simple mTLS)
```bash
# Make HTTPS request with client certificate
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .
```

**Expected output:**
```json
{
  "status": "UP",
  "description": "PKI Authentication successful"
}
```

**If you see SSL errors:**
- Check certificates exist: `ls certs/`
- Check paths are correct (relative from project root)
- Try with verbose: Add `-v` flag to curl command

### Step 4.4: Test 2 - Get Certificate Information
```bash
# Request certificate info from the server
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info | jq .
```

**Expected output should include:**
```json
{
  "subject": "CN=...",
  "issuer": "O=...",
  "validFrom": "...",
  "validTo": "...",
  "authenticated": true
}
```

### Step 4.5: Test 3 - Verify Certificate Validity
```bash
# Test certificate verification endpoint
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify | jq .
```

**Expected output:**
```json
{
  "valid": true,
  "reason": "Certificate is valid and trusted"
}
```

### Step 4.6: Test 4 - Access Secure Data with PKI
```bash
# Access a protected resource using mTLS
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data | jq .
```

**Expected:** Should return secure data (proof of successful mTLS)

### Step 4.7: Test 5 - Determine Authentication Method Used
```bash
# Check which authentication method was used
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method | jq .
```

**Expected output:**
```json
{
  "method": "PKI",
  "details": "Client certificate detected and validated"
}
```

---

## Phase 5: Test Negative Scenarios (Security Tests)

### Step 5.1: Request Without Client Certificate (Should Fail)
```bash
# Try to access PKI endpoint without client cert
curl -s -k https://localhost:8443/api/pki/health 2>&1
```

**Expected:** Should fail with SSL/TLS error or 401/403

### Step 5.2: Request with Wrong Certificate (Should Fail)
```bash
# Create a fake certificate (for testing)
# This should be rejected by the server

# Try with a self-signed cert not signed by our CA
openssl req -x509 -newkey rsa:4096 -nodes \
  -keyout /tmp/fake-key.pem -out /tmp/fake-cert.pem \
  -days 1 -subj "/CN=fake"

# Try to use it
curl -s --cert /tmp/fake-cert.pem \
     --key /tmp/fake-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health 2>&1
```

**Expected:** Connection should fail (untrusted certificate)

### Step 5.3: Request with Invalid CA Certificate (Should Fail)
```bash
# Try with a different CA certificate
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert /tmp/fake-cert.pem \
     https://localhost:8443/api/pki/health 2>&1
```

**Expected:** SSL certificate verification failure

---

## Phase 6: Test Dual Authentication (OAuth2 + PKI)

### Step 6.1: PKI Endpoints Require Certificate
```bash
# PKI endpoints should require certificate
# These should FAIL without certificate:
curl -s -k -H "Authorization: Bearer $TOKEN" \
  https://localhost:8443/api/pki/health 2>&1 | head -5
```

**Expected:** Fails because PKI endpoints only accept mTLS

### Step 6.2: OAuth2 Endpoints Require Token
```bash
# OAuth2 endpoints should require JWT token
# This should FAIL without token:
curl -s https://localhost:8443/api/oauth2/test 2>&1
```

**Expected:** 401 Unauthorized

### Step 6.3: General Endpoints Accept Either Auth
```bash
# General endpoints accept either PKI or OAuth2

# Test with PKI certificate
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/users | jq . | head -10

# Test with OAuth2 token
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users | jq . | head -10
```

**Expected:** Both should work and return user data

---

## Phase 7: Advanced Testing (Optional)

### Step 7.1: View Certificate Chain
```bash
# See the complete certificate chain
openssl s_client -connect localhost:8443 \
  -cert certs/client-cert.pem \
  -key certs/client-key.pem \
  -showcerts < /dev/null 2>&1 | grep -A5 "subject="
```

### Step 7.2: Test with curl -v (Verbose)
```bash
# See detailed TLS handshake
curl -v --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health 2>&1 | grep -i "ssl\|certificate\|verify"
```

### Step 7.3: Check Certificate Expiration
```bash
# Check when certificates expire
echo "=== CA Certificate ==="
openssl x509 -in certs/ca-cert.pem -noout -dates

echo "=== Server Certificate ==="
openssl x509 -in certs/server-cert.pem -noout -dates

echo "=== Client Certificate ==="
openssl x509 -in certs/client-cert.pem -noout -dates
```

### Step 7.4: View Keystore Contents
```bash
# See what's in the keystore
keytool -list -v -keystore certs/keystore.p12 \
  -storetype PKCS12 -storepass changeit | head -50
```

### Step 7.5: View Truststore Contents
```bash
# See what's in the truststore
keytool -list -v -keystore certs/truststore.p12 \
  -storetype PKCS12 -storepass changeit
```

---

## Phase 8: Stop and Cleanup

### Step 8.1: Stop the Application
```bash
# If running in foreground, press Ctrl+C
# If running in background, find and kill the process:
pkill -f "spring-boot:run"
# Or
pkill -f "secure-project-hub"
```

### Step 8.2: Stop PostgreSQL (if using Docker)
```bash
# Stop the database
docker-compose down

# Or just stop without removing
docker-compose stop
```

---

## Quick Reference: All Test Commands

### OAuth2 Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')
echo $TOKEN
```

### PKI Health Check
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .
```

### PKI Certificate Info
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info | jq .
```

### PKI Verify
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify | jq .
```

### PKI Secure Data
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data | jq .
```

### Check Auth Method
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method | jq .
```

---

## Troubleshooting

### Issue: Certificate verification failed
```
curl: (60) SSL certificate problem: unable to get local issuer certificate
```
**Solution:**
- Ensure CA certificate file exists: `ls certs/ca-cert.pem`
- Use correct path (relative from project root)
- Try with `-k` flag temporarily to skip verification: `curl -k ...`

### Issue: Client certificate not found
```
curl: (54) Connection reset by peer
```
**Solution:**
- Check files exist: `ls certs/client-cert.pem certs/client-key.pem`
- Use correct paths
- Check file permissions: `chmod 644 certs/*.pem`

### Issue: Port already in use
```
Address already in use
```
**Solution:**
```bash
# Find process using port 8443
lsof -i :8443

# Kill it
kill -9 <PID>
```

### Issue: Application won't start
```bash
# Check logs:
tail -50 /var/log/secure-project-hub.log

# Try with verbose output:
mvn spring-boot:run -X
```

---

## Success Criteria

After completing this guide, you should be able to:

✅ Build and run the application with PKI support  
✅ Successfully authenticate using mTLS with client certificate  
✅ Access PKI-protected endpoints (`/api/pki/*`)  
✅ Get certificate information from the application  
✅ Verify that PKI and OAuth2 work independently  
✅ Confirm security (unauthenticated requests fail)  
✅ See certificate details and expiration dates  

---

## Next Steps

1. **Production Deployment:** Use real certificates from a trusted CA
2. **Certificate Rotation:** Implement automated certificate renewal
3. **Monitoring:** Set up alerts for certificate expiration
4. **Load Testing:** Test PKI with multiple concurrent connections
5. **Integration:** Add PKI to service-to-service communication
