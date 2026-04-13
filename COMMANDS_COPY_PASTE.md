# PKI Testing Commands - Copy & Paste Ready

## All Commands for Testing PKI on Port 8443

**Start from project root:**
```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
```

---

## Command 1: Check HTTP Health
```bash
curl -s http://localhost:8080/actuator/health | jq .
```
**Expected:** `"status": "UP"`

---

## Command 2: Get OAuth2 Token
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')
echo "Token: $TOKEN"
```
**Expected:** Long JWT token starting with `eyJ...`

---

## Command 3: PKI Health Check ⭐
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health | jq .
```
**Expected:**
```json
{
  "status": "UP",
  "description": "PKI Authentication successful"
}
```

---

## Command 4: Get Certificate Details ⭐
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info | jq .
```
**Expected:** Certificate subject, issuer, validity dates

---

## Command 5: Verify Certificate ⭐
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify | jq .
```
**Expected:**
```json
{
  "valid": true,
  "reason": "Certificate is valid and trusted"
}
```

---

## Command 6: Access Secure Data ⭐
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data | jq .
```
**Expected:** Secure data payload

---

## Command 7: Check Auth Method ⭐
```bash
curl -s --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method | jq .
```
**Expected:**
```json
{
  "method": "PKI",
  "details": "Client certificate detected and validated"
}
```

---

## Command 8: Security Test - Request WITHOUT Certificate (Should Fail) ⚠️
```bash
curl -s -k https://localhost:8443/api/pki/health 2>&1
```
**Expected:** Connection error or SSL/TLS failure

---

## Command 9: View Client Certificate Details
```bash
openssl x509 -in certs/client-cert.pem -text -noout | head -30
```
**Expected:** Certificate details (subject, issuer, validity, public key)

---

## Command 10: Check All Certificate Expiration Dates
```bash
echo "=== CA Certificate ===" && openssl x509 -in certs/ca-cert.pem -noout -dates
echo "=== Server Certificate ===" && openssl x509 -in certs/server-cert.pem -noout -dates
echo "=== Client Certificate ===" && openssl x509 -in certs/client-cert.pem -noout -dates
```
**Expected:** Three pairs of notBefore/notAfter dates

---

## Docker Commands

### Check Docker Containers
```bash
docker ps | grep secure-project-hub
```
**Expected:** Both app and postgres containers running

### View Logs (Live)
```bash
docker logs -f secure-project-hub-app-1
```
**Note:** Press Ctrl+C to exit

### Restart App
```bash
docker restart secure-project-hub-app-1
```

### Check Health Status
```bash
docker ps
```
**Look for:** Container status = "Up X minutes (healthy)"

---

## Run All Tests Automatically

We've created a test suite for you:

```bash
chmod +x pki-test-suite.sh
bash pki-test-suite.sh
```

This runs all tests and shows a summary with ✅ PASSED or ❌ FAILED

---

## One-Liner: Test All 5 PKI Endpoints

```bash
for endpoint in health certificate-info verify secure-data auth-method; do \
  echo "=== Testing: $endpoint ===" && \
  curl -s --cert certs/client-cert.pem --key certs/client-key.pem --cacert certs/ca-cert.pem https://localhost:8443/api/pki/$endpoint | jq . && \
  echo ""; \
done
```

---

## Troubleshooting

### If certificate fails to load:
Check that you're in the right directory:
```bash
pwd  # Should show: /Users/jguo/work/eclipse-workspace/secure-project-hub
ls certs/  # Should list all certificate files
```

### If you get "Port already in use":
```bash
lsof -i :8443
kill -9 <PID>
docker restart secure-project-hub-app-1
```

### If you get SSL errors:
Try with verbose output:
```bash
curl -v --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health 2>&1 | grep -i "ssl\|certificate\|verify"
```

### If Docker container is unhealthy:
```bash
docker logs secure-project-hub-app-1 --tail 50
docker restart secure-project-hub-app-1
sleep 30
docker ps  # Check status
```

---

## What's Being Tested

| Test | Purpose | Auth Method |
|------|---------|-------------|
| HTTP Health | Verify app is running | None |
| OAuth2 Token | Get JWT token | Username/Password |
| PKI Health | Verify HTTPS + certificate | Client Certificate |
| Certificate Info | Get cert details from server | Client Certificate |
| Verify Certificate | Confirm cert is valid | Client Certificate |
| Secure Data | Access protected resource | Client Certificate |
| Auth Method | Confirm PKI auth detected | Client Certificate |
| Security Test | Verify requests fail without cert | None (should fail) |

---

## Success = All Tests Pass ✅

If you see:
- ✅ All endpoints return 200 OK
- ✅ Certificate info is correct
- ✅ Security test fails (as expected)
- ✅ Auth method shows "PKI"

Then **PKI/mTLS is working perfectly!** 🎉

---

## Need More Details?

See these files:
- `PKI_QUICK_START.md` - Overview & tips
- `PKI_TESTING_GUIDE_DOCKER.md` - Detailed guide with 9 phases
- `PKI_TESTING_GUIDE.md` - General testing guide
- `pki-test-suite.sh` - Automated test runner
- `pki-test-quick-ref.sh` - Command reference

---

**App Status:** Running on Docker ✅  
**Port:** 8443 (HTTPS with mTLS)  
**Ready to Test:** Yes, copy commands above!
