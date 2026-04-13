# PKI Testing - Docker Quick Start (Port 8443)

Your app is running in Docker on port **8443** (HTTPS with PKI/mTLS support).

---

## 📋 Quick Summary

| Item | Value |
|------|-------|
| **HTTP Port** | 8080 |
| **HTTPS Port** | 8443 |
| **Protocol** | HTTPS with mTLS (mutual TLS) |
| **App Container** | secure-project-hub-app-1 |
| **DB Container** | secure-project-hub-postgres-1 |
| **Client Certificate** | certs/client-cert.pem |
| **Client Key** | certs/client-key.pem |
| **CA Certificate** | certs/ca-cert.pem |

---

## 🚀 Quick Test Commands

All commands should be run from the project root directory:

```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
```

### Test 1: HTTP Health Check
```bash
curl -s http://localhost:8080/actuator/health | jq .
```

### Test 2: Get OAuth2 Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')
echo $TOKEN
```

### Test 3: PKI Health Check (mTLS)
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .
```

### Test 4: Get Certificate Info
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info | jq .
```

### Test 5: Verify Certificate
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify | jq .
```

### Test 6: Access Secure Data
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data | jq .
```

### Test 7: Check Auth Method
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method | jq .
```

---

## 🧪 Run All Tests at Once

We've created an automated test suite for you:

```bash
# Make it executable
chmod +x pki-test-suite.sh

# Run all tests
bash pki-test-suite.sh
```

This will:
- ✅ Test HTTP health
- ✅ Get OAuth2 token
- ✅ Test all 5 PKI endpoints
- ✅ Verify security (unauthenticated requests fail)
- ✅ Show certificate details
- ✅ Check Docker status
- ✅ Provide pass/fail summary

---

## 📚 Documentation Files

We've created comprehensive guides for you:

1. **PKI_TESTING_GUIDE_DOCKER.md** (recommended)
   - Complete Docker-specific testing guide
   - 9 phases with detailed explanations
   - Troubleshooting section

2. **PKI_TESTING_GUIDE.md**
   - General guide (can be adapted for Docker)
   - 8 phases covering all aspects
   - Advanced certificate inspection

3. **pki-test-quick-ref.sh**
   - Quick reference of all commands
   - Categorized by phase
   - Easy to copy/paste

4. **pki-test-suite.sh** (auto-run)
   - Automated test suite
   - Run and get pass/fail results

---

## 🔍 Expected Results

### ✅ PKI Health Check Should Return:
```json
{
  "status": "UP",
  "description": "PKI Authentication successful"
}
```

### ✅ Certificate Info Should Return:
```json
{
  "subject": "CN=client",
  "issuer": "O=SecureProjectHub",
  "validFrom": "2024-...",
  "validTo": "2034-...",
  "authenticated": true
}
```

### ✅ Verify Should Return:
```json
{
  "valid": true,
  "reason": "Certificate is valid and trusted"
}
```

### ✅ Security Test Should FAIL:
```
curl: (60) SSL certificate problem
# OR
SSL certificate required
```

---

## 🐳 Docker Commands Reference

### Check if containers are running
```bash
docker ps | grep secure-project-hub
```

### View live logs
```bash
docker logs -f secure-project-hub-app-1
```

### Restart the app
```bash
docker restart secure-project-hub-app-1
```

### Check container health
```bash
docker inspect secure-project-hub-app-1 --format='{{json .State.Health}}' | jq .
```

### View port mappings
```bash
docker port secure-project-hub-app-1
```

---

## 🎯 What You're Testing

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/api/pki/health` | GET | mTLS | Health check with certificate |
| `/api/pki/certificate-info` | GET | mTLS | Get certificate details |
| `/api/pki/verify` | GET | mTLS | Verify certificate validity |
| `/api/pki/secure-data` | GET | mTLS | Access protected resource |
| `/api/pki/auth-method` | GET | mTLS | Confirm PKI auth method |

All require:
- `--cert certs/client-cert.pem` (client certificate)
- `--key certs/client-key.pem` (client private key)
- `--cacert certs/ca-cert.pem` (CA certificate for verification)

---

## ⚠️ Common Issues & Solutions

### Issue: Connection refused on 8443
```
curl: (7) Failed to connect to localhost port 8443
```
**Solution:** Check Docker container is running
```bash
docker ps | grep secure-project-hub-app
```

### Issue: SSL certificate verification failed
```
curl: (60) SSL certificate problem
```
**Solution:** Ensure you're using correct certificates:
```bash
ls -la certs/client-cert.pem certs/client-key.pem certs/ca-cert.pem
```

### Issue: No client certificate error
```
SSL_ERROR_RX_RECORD_TOO_LONG or peer closed connection
```
**Solution:** Make sure you include both `--cert` and `--key`:
```bash
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health
```

### Issue: Certificate verification should fail but doesn't
**Try with a fake certificate:**
```bash
# Create fake cert
openssl req -x509 -newkey rsa:4096 -nodes \
  -keyout /tmp/fake-key.pem -out /tmp/fake-cert.pem \
  -days 1 -subj "/CN=fake"

# This should fail:
curl -s --cert /tmp/fake-cert.pem \
     --key /tmp/fake-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health
```

---

## ✅ Success Checklist

After running tests, verify:

- [ ] HTTP health check returns UP
- [ ] OAuth2 token obtained successfully
- [ ] PKI health check returns UP with "PKI Authentication successful"
- [ ] Certificate info shows correct subject and issuer
- [ ] Certificate verify returns valid:true
- [ ] Secure data endpoint returns data
- [ ] Auth method shows "PKI"
- [ ] Request without certificate fails (security check)
- [ ] All Docker containers running

---

## 📞 Next Steps

1. **Run the automated test suite first:**
   ```bash
   bash pki-test-suite.sh
   ```

2. **If all tests pass:** PKI is working! 🎉

3. **If tests fail:** Check the detailed guide:
   ```
   PKI_TESTING_GUIDE_DOCKER.md
   ```

4. **View logs for debugging:**
   ```bash
   docker logs -f secure-project-hub-app-1
   ```

5. **Ready for production?** See Phase 8 in PKI_TESTING_GUIDE_DOCKER.md

---

## 📊 Architecture Overview

```
Your Machine (Host)
    ├─ Port 8080 ────→ Docker Container Port 8080 (HTTP)
    │                   Secure Project Hub App
    │                   (Spring Boot)
    │
    ├─ Port 8443 ────→ Docker Container Port 8443 (HTTPS/mTLS)
    │                   ├─ Requires: Client Certificate
    │                   ├─ Validates: Against CA Certificate
    │                   └─ PKI Endpoints: /api/pki/**
    │
    ├─ Port 5432 ────→ Docker Container Port 5432
    │                   PostgreSQL Database
    │
    ├─ certs/ ────────→ In Docker Container at /app/
                        ├─ keystore.p12 (server side)
                        └─ truststore.p12 (server side)
```

---

## 🎓 About PKI/mTLS

**PKI (Public Key Infrastructure) / mTLS (Mutual TLS)** is authentication using:
- Client Certificate: Proves client identity
- Server Certificate: Proves server identity
- CA Certificate: Validates the certificate chain

Unlike OAuth2 (token-based), PKI doesn't use passwords. The certificate IS the credential.

**Use PKI for:**
- Service-to-service communication
- Highly secure internal systems
- Compliance requirements (banking, healthcare)

**Advantages:**
- ✅ No passwords needed
- ✅ Certificates can be revoked immediately
- ✅ Mutual verification (both sides prove identity)
- ✅ Suitable for automated systems

---

**Created:** April 13, 2026  
**App Status:** Running in Docker ✅  
**Ready to Test:** PKI/mTLS on port 8443 ✅
