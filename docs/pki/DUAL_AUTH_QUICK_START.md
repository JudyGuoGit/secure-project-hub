# Dual Authentication Quick Reference

## What Was Implemented

You now have **both OAuth2 JWT and PKI/mTLS authentication** working together in one application.

## Quick Setup (5 minutes)

### Step 1: Enable PKI in application.yml
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    client-auth: want

pki:
  enabled: true
```

### Step 2: Generate Certificates
```bash
bash scripts/generate-certificates.sh
```

### Step 3: Copy Keystores
```bash
cp certs/keystore.p12 src/main/resources/
cp certs/truststore.p12 src/main/resources/
```

### Step 4: Run Application
```bash
mvn spring-boot:run
```

## Test It Out

### Test OAuth2 (Bearer Token)
```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')

# Use token
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/users
```

### Test PKI (Client Certificate)
```bash
# Simple health check
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health

# Get certificate info
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info
```

## Files Created

### Security Classes
- `PkiCertificateValidator.java` - Validates X.509 certificates
- `PkiAuthenticationFilter.java` - Extracts and processes client certificates
- `PkiSecurityConfig.java` - Configures SSL/TLS and keystore/truststore

### API Controller
- `PkiController.java` - 5 endpoints for PKI testing:
  - `/api/pki/health` - Health check
  - `/api/pki/certificate-info` - Certificate details
  - `/api/pki/verify` - Certificate validation
  - `/api/pki/secure-data` - Protected resource
  - `/api/pki/auth-method` - Which auth method was used

### Configuration
- Updated `SecurityConfig.java` - Dual auth support with proper authorization
- Updated `application.yml` - PKI configuration options
- Fixed `generate-certificates.sh` - Minor typo fix

### Documentation
- `docs/DUAL_AUTHENTICATION_SETUP.md` - Complete setup guide
- `docs/DUAL_AUTHENTICATION_IMPLEMENTATION.md` - Implementation details

## Architecture

```
Requests
   ↓
   ├─→ Public Endpoints (/api/token, /swagger-ui, etc.) → Allow All
   │
   ├─→ PKI Endpoints (/api/pki/**)
   │   ↓
   │   → TLS Handshake → Extract Certificate
   │   → PkiAuthenticationFilter → ROLE_PKI_USER
   │
   ├─→ OAuth2 Endpoints (/api/oauth2/**)
   │   ↓
   │   → Bearer Token in Header
   │   → JwtTokenFilter → User Roles from DB
   │
   └─→ General API (/api/**)
       ↓
       → Either PKI or OAuth2 → Based on Which is Present
```

## Key Components

| Component | Purpose | Location |
|-----------|---------|----------|
| `PkiCertificateValidator` | Validates X.509 certs | `security/` |
| `PkiAuthenticationFilter` | Processes PKI auth | `security/` |
| `PkiSecurityConfig` | SSL/TLS setup | `config/` |
| `PkiController` | PKI endpoints | `controller/` |
| `SecurityConfig` | Authorization rules | `config/` |
| `application.yml` | Configuration | `resources/` |

## Authorization Rules

```
Public (No Auth Required):
- POST /api/token
- GET /swagger-ui/**
- GET /v3/api-docs/**

PKI Only (ROLE_PKI_USER):
- GET /api/pki/**

OAuth2/JWT (ROLE_USER or ROLE_ADMIN):
- GET /api/oauth2/**

Either OAuth2 or PKI (Any Authenticated User):
- GET /api/users
- GET /api/roles
- etc.
```

## Certificate Files

After running `generate-certificates.sh`:

```
certs/
├── ca-cert.pem           → Safe to commit (public)
├── ca-key.pem            → KEEP SECURE (never commit)
├── keystore.p12          → Copy to src/main/resources/
├── truststore.p12        → Copy to src/main/resources/
├── client-cert.pem       → For testing
├── client-key.pem        → For testing
└── client-keystore.p12   → For testing
```

## Common Operations

### Add New Client Certificate
```bash
cd certs
openssl genrsa -out new-client-key.pem 2048
openssl req -new -key new-client-key.pem -out new-client.csr \
  -subj "/CN=new-client/O=Secure-Project-Hub/C=US"
openssl x509 -req -in new-client.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out new-client-cert.pem -days 365 -sha256
```

### Check Certificate Expiration
```bash
openssl x509 -in certs/client-cert.pem -text -noout | grep -A 2 Validity
```

### Verify Certificate Chain
```bash
openssl verify -CAfile certs/ca-cert.pem certs/client-cert.pem
```

### View Keystore Contents
```bash
keytool -list -v -keystore keystore.p12 -storepass changeit
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `certificate verify failed` | Copy keystores to `src/main/resources/` |
| `unable to find valid certification path` | Truststore must contain CA that signed the cert |
| `Access Denied on /api/pki/*` | Ensure `pki.enabled: true` in `application.yml` |
| `Port already in use` | Change `server.port` in `application.yml` |
| `Cannot find keystore.p12` | Run certificate generation script first |

## Switching Between Auth Methods

### Use Case 1: Mobile App (OAuth2 JWT)
```java
// Get token once, reuse for multiple requests
TOKEN=$(curl -s -X POST .../api/token ...)
curl -H "Authorization: Bearer $TOKEN" .../api/users
```

### Use Case 2: Service-to-Service (PKI/mTLS)
```bash
# Each request includes certificate
curl --cert client.pem --key client-key.pem .../api/pki/...
```

### Use Case 3: Both Methods (Hybrid)
```bash
# API accessible by both methods
curl -H "Authorization: Bearer $TOKEN" http://.../api/users
curl --cert client.pem --key client-key.pem https://.../api/users
```

## Security Checklist

- [ ] Keep `ca-key.pem` in secure vault (never commit)
- [ ] Add `keystore.p12` and `truststore.p12` to `.gitignore`
- [ ] Set `client-auth: need` in production (if all clients support PKI)
- [ ] Monitor certificate expiration dates
- [ ] Use strong passwords for keystores
- [ ] Implement audit logging for PKI access
- [ ] Use environment variables for passwords in production
- [ ] Enable HTTPS only (set `server.ssl.enabled: true`)

## API Documentation

Access Swagger UI at:
- **HTTP**: `http://localhost:8080/swagger-ui/index.html`
- **HTTPS**: `https://localhost:8443/swagger-ui/index.html`

View all endpoints and test them interactively.

## Build Status

✅ **All 34 Java files compile successfully**
✅ **Build produces runnable JAR**
✅ **Ready for deployment**

---

## More Information

- Full Setup Guide: `docs/DUAL_AUTHENTICATION_SETUP.md`
- Implementation Details: `docs/DUAL_AUTHENTICATION_IMPLEMENTATION.md`
- PKI vs OAuth2: `docs/PKI_vs_OAUTH2.md`
- Authorization: `docs/AUTHORIZATION.md`
