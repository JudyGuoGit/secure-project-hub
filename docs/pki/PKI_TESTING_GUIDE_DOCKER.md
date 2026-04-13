# PKI Testing Guide - Docker Version

This guide provides all commands to test PKI/mTLS authentication with the app running in Docker.

---

## Prerequisites

- Docker and Docker Compose installed
- `curl` installed (for making requests)
- `openssl` installed (to inspect certificates)
- `jq` installed (optional but recommended for JSON parsing)

Check what's running:
```bash
docker ps
```

Expected output should show both `secure-project-hub-app-1` and `secure-project-hub-postgres-1` containers.

---

## Phase 1: Verify Docker Setup

### Step 1.1: Check App Container Status
```bash
docker ps | grep secure-project-hub-app
```

Expected: Container should be running (status: `Up X minutes`)

### Step 1.2: Check PostgreSQL Container
```bash
docker ps | grep postgres
```

Expected: PostgreSQL should be running and healthy

### Step 1.3: View Application Logs
```bash
# View last 100 lines of logs
docker logs secure-project-hub-app-1 --tail 100

# Follow logs in real-time (press Ctrl+C to exit)
docker logs -f secure-project-hub-app-1
```

### Step 1.4: Verify Ports Are Accessible
```bash
# Check if ports are listening
netstat -an | grep -E '8080|8443|5432'

# Or using lsof
lsof -i :8080
lsof -i :8443
lsof -i :5432
```

---

## Phase 2: Test HTTP Endpoints (Baseline)

### Step 2.1: Health Check on HTTP
```bash
curl -s http://localhost:8080/actuator/health | jq .
```

Expected:
```json
{
  "status": "UP"
}
```

### Step 2.2: Check App is Ready
```bash
curl -s http://localhost:8080/actuator/info | jq .
```

---

## Phase 3: Get OAuth2 Token (Baseline)

### Step 3.1: Obtain OAuth2 Token
```bash
# Get a JWT token using OAuth2
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token // .access_token')

echo "OAuth2 Token obtained: $TOKEN"
```

Expected: You should see a JWT token (starts with `eyJ...`)

### Step 3.2: Verify Token Works
```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users | jq . | head -20
```

Expected: Should return list of users

---

## Phase 4: Test PKI/mTLS with Docker

### Step 4.1: Verify Certificates Exist on Host
```bash
# Check certificate files in your project directory
ls -lh /Users/jguo/work/eclipse-workspace/secure-project-hub/certs/
```

Expected files:
- `ca-cert.pem` (CA certificate - public)
- `ca-key.pem` (CA private key - secret)
- `client-cert.pem` (Client certificate)
- `client-key.pem` (Client private key)
- `server-cert.pem` (Server certificate)
- `server-key.pem` (Server private key)

### Step 4.2: Test 1 - PKI Health Check via HTTPS
```bash
# Navigate to your project directory
cd /Users/jguo/work/eclipse-workspace/secure-project-hub

# Make HTTPS request with client certificate
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .
```

Expected:
```json
{
  "status": "UP",
  "description": "PKI Authentication successful"
}
```

### Step 4.3: Test 2 - Get Certificate Information
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info | jq .
```

Expected:
```json
{
  "subject": "CN=client",
  "issuer": "O=SecureProjectHub",
  "validFrom": "2024-...",
  "validTo": "2034-...",
  "authenticated": true
}
```

### Step 4.4: Test 3 - Verify Certificate
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify | jq .
```

Expected:
```json
{
  "valid": true,
  "reason": "Certificate is valid and trusted"
}
```

### Step 4.5: Test 4 - Secure Data Access
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data | jq .
```

Expected: Returns secure data (confirms mTLS authentication)

### Step 4.6: Test 5 - Check Auth Method
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method | jq .
```

Expected:
```json
{
  "method": "PKI",
  "details": "Client certificate detected and validated"
}
```

---

## Phase 5: Security Tests (Negative Scenarios)

### Step 5.1: Request WITHOUT Certificate (Should Fail)
```bash
# Try accessing PKI endpoint without client certificate
curl -s -k https://localhost:8443/api/pki/health 2>&1 | head -20
```

Expected: Should fail with SSL/TLS error or HTTP 401/403

### Step 5.2: Request with Wrong/Self-Signed Certificate (Should Fail)
```bash
# Create a fake self-signed certificate
openssl req -x509 -newkey rsa:4096 -nodes \
  -keyout /tmp/fake-key.pem -out /tmp/fake-cert.pem \
  -days 1 -subj "/CN=fake"

# Try to use it
curl -s --cert /tmp/fake-cert.pem \
     --key /tmp/fake-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health 2>&1
```

Expected: Connection fails (untrusted certificate)

### Step 5.3: Request with Invalid CA (Should Fail)
```bash
# Try with wrong CA certificate
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert /tmp/fake-cert.pem \
     https://localhost:8443/api/pki/health 2>&1
```

Expected: SSL certificate verification failure

---

## Phase 6: Dual Authentication Tests

### Step 6.1: PKI Endpoints (Certificate Only)
```bash
# PKI endpoints should ONLY work with certificate
# This should WORK:
echo "=== With Certificate (should work) ==="
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .

# This should FAIL:
echo "=== Without Certificate (should fail) ==="
curl -s -k https://localhost:8443/api/pki/health 2>&1 | head -5
```

### Step 6.2: OAuth2 Endpoints (Token Only)
```bash
# OAuth2 endpoints should ONLY work with token
# This should WORK:
echo "=== With Token (should work) ==="
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/oauth2/protected 2>&1 | jq . | head -10

# This should FAIL:
echo "=== Without Token (should fail) ==="
curl -s http://localhost:8080/api/oauth2/protected 2>&1 | head -5
```

### Step 6.3: General Endpoints (Either Auth Works)
```bash
# General endpoints should work with EITHER PKI or OAuth2

# Test with PKI certificate:
echo "=== General endpoint with PKI ==="
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/users | jq . | head -10

# Test with OAuth2 token:
echo "=== General endpoint with OAuth2 ==="
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/users | jq . | head -10
```

---

## Phase 7: Advanced Certificate Inspection

### Step 7.1: Inspect Client Certificate
```bash
openssl x509 -in certs/client-cert.pem -text -noout | head -50
```

Shows: Certificate subject, issuer, validity dates, public key

### Step 7.2: Inspect Server Certificate
```bash
openssl x509 -in certs/server-cert.pem -text -noout | head -50
```

### Step 7.3: Check Certificate Expiration
```bash
echo "=== CA Certificate ==="
openssl x509 -in certs/ca-cert.pem -noout -dates

echo "=== Server Certificate ==="
openssl x509 -in certs/server-cert.pem -noout -dates

echo "=== Client Certificate ==="
openssl x509 -in certs/client-cert.pem -noout -dates
```

### Step 7.4: View Keystore (Server Side)
```bash
# View server keystore (in Docker container)
docker exec secure-project-hub-app-1 \
  keytool -list -v -keystore /app/keystore.p12 \
    -storetype PKCS12 -storepass changeit
```

### Step 7.5: View Truststore (Server Side)
```bash
# View server truststore (in Docker container)
docker exec secure-project-hub-app-1 \
  keytool -list -v -keystore /app/truststore.p12 \
    -storetype PKCS12 -storepass changeit
```

---

## Phase 8: Docker Container Management

### Step 8.1: View Detailed Container Info
```bash
docker inspect secure-project-hub-app-1 | jq . | head -100
```

### Step 8.2: Check Container Health
```bash
docker inspect secure-project-hub-app-1 --format='{{json .State.Health}}' | jq .
```

### Step 8.3: Restart the Application
```bash
# Restart the app (keeps Docker running)
docker restart secure-project-hub-app-1

# Wait for it to be ready (~30 seconds)
sleep 30

# Verify it's up
docker ps | grep secure-project-hub-app
```

### Step 8.4: Stop Containers
```bash
# Stop but keep running (pause)
docker-compose stop

# Start again
docker-compose start
```

### Step 8.5: Full Cleanup and Restart
```bash
# Remove containers and rebuild
docker-compose down
docker-compose up -d

# Wait for startup (60 seconds)
sleep 60

# Check status
docker ps
```

### Step 8.6: View Docker Network
```bash
# See the secure-network bridge
docker network inspect secure-project-hub_secure-network
```

---

## Phase 9: Docker Debugging

### Step 9.1: Real-time Log Following
```bash
# Follow logs in real-time
docker logs -f secure-project-hub-app-1

# Press Ctrl+C to exit
```

### Step 9.2: Filter Logs by Keyword
```bash
# Show only SSL/TLS related logs
docker logs secure-project-hub-app-1 --tail 200 | grep -i "ssl\|tls\|certificate\|pki"

# Show only errors
docker logs secure-project-hub-app-1 --tail 200 | grep -i "error\|exception"
```

### Step 9.3: Execute Command Inside Container
```bash
# Open shell in container
docker exec -it secure-project-hub-app-1 /bin/bash

# Inside container, you can:
# - Check certificates: ls -la /app/
# - View environment: env | grep SPRING
# - Check Java: java -version
# - Exit: exit
```

### Step 9.4: Check Port Mapping
```bash
# See which host ports map to container ports
docker port secure-project-hub-app-1
```

Expected:
```
5005/tcp -> 0.0.0.0:5005
8080/tcp -> 0.0.0.0:8080
8443/tcp -> 0.0.0.0:8443
```

---

## Quick Reference: Essential Commands

### Get OAuth2 Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')
```

### Test PKI Health
```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .
```

### Test PKI All Endpoints
```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub

echo "1. Health:"
curl -s --cert certs/client-cert.pem --key certs/client-key.pem --cacert certs/ca-cert.pem https://localhost:8443/api/pki/health | jq .

echo -e "\n2. Certificate Info:"
curl -s --cert certs/client-cert.pem --key certs/client-key.pem --cacert certs/ca-cert.pem https://localhost:8443/api/pki/certificate-info | jq .

echo -e "\n3. Verify:"
curl -s --cert certs/client-cert.pem --key certs/client-key.pem --cacert certs/ca-cert.pem https://localhost:8443/api/pki/verify | jq .

echo -e "\n4. Secure Data:"
curl -s --cert certs/client-cert.pem --key certs/client-key.pem --cacert certs/ca-cert.pem https://localhost:8443/api/pki/secure-data | jq .

echo -e "\n5. Auth Method:"
curl -s --cert certs/client-cert.pem --key certs/client-key.pem --cacert certs/ca-cert.pem https://localhost:8443/api/pki/auth-method | jq .
```

### View App Logs
```bash
docker logs -f secure-project-hub-app-1
```

### Check Docker Status
```bash
docker ps
docker ps -a
```

### Restart Docker App
```bash
docker restart secure-project-hub-app-1
```

---

## Troubleshooting

### Issue: "Connection refused" on port 8443
```
curl: (7) Failed to connect to localhost port 8443
```
**Solution:**
```bash
# Check if Docker container is running
docker ps | grep secure-project-hub-app

# If not running, start it
docker-compose up -d

# Wait 30-60 seconds for startup
```

### Issue: Certificate verification failed
```
curl: (60) SSL certificate problem
```
**Solution:**
- Ensure certificates exist: `ls certs/*.pem`
- Use correct paths (run curl from project root)
- Check CA certificate: `openssl x509 -in certs/ca-cert.pem -noout`

### Issue: "Client certificate required"
```
SSL_ERROR_RX_RECORD_TOO_LONG or peer closed connection
```
**Solution:**
- Ensure you're using `--cert` and `--key` flags
- Verify certificate is valid: `openssl x509 -in certs/client-cert.pem -noout`

### Issue: Container unhealthy status
```
docker ps shows: "Up 5 minutes (unhealthy)"
```
**Solution:**
```bash
# Check logs
docker logs secure-project-hub-app-1 --tail 50

# Wait longer for startup (can take 60 seconds)
sleep 60

# Restart container
docker restart secure-project-hub-app-1
```

### Issue: Cannot find certificates in Docker container
**Solution:**
- Certificates are on your HOST machine in `certs/` folder
- You use them from your HOST to connect to the Docker container
- The container copies them during build (see Dockerfile)

---

## Success Criteria

After completing this guide, you should be able to:

✅ Verify Docker containers are running  
✅ Access app on http://localhost:8080 and https://localhost:8443  
✅ Obtain OAuth2 token via HTTP  
✅ Successfully authenticate using mTLS with client certificate  
✅ Access all 5 PKI endpoints (`/api/pki/*`)  
✅ Verify security (unauthenticated requests fail)  
✅ View certificate details and expiration  
✅ Manage and debug Docker containers  
✅ View and follow application logs  

---

## Common HTTP Ports Reference

| Service | Port | Protocol | Use |
|---------|------|----------|-----|
| App (HTTP) | 8080 | HTTP | OAuth2, general API |
| App (HTTPS) | 8443 | HTTPS | PKI/mTLS, SSL/TLS |
| PostgreSQL | 5432 | TCP | Database |
| Debug | 5005 | TCP | Java remote debugging |

---

## Next Steps

1. **Run all PKI tests** using Phase 4-6 commands
2. **Verify security** using negative tests in Phase 5
3. **Inspect certificates** using Phase 7
4. **Monitor logs** using Phase 9 for troubleshooting
5. **Test in production** after successful Docker tests
