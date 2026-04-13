# Dual Authentication Implementation - Summary

**Date:** April 13, 2026  
**Project:** Secure Project Hub - Dual OAuth2 JWT + PKI/mTLS Authentication  
**Status:** ✅ Implementation Complete

---

## Overview

The Secure Project Hub has been successfully enhanced with **dual authentication support**, enabling both OAuth2 JWT and PKI/mTLS authentication methods to coexist in the same application. This allows different authentication strategies for different use cases:

- **OAuth2 JWT**: User-facing applications, mobile apps, third-party integrations
- **PKI/mTLS**: Service-to-service communication, highly secure internal systems, compliance requirements

---

## Implementation Details

### 1. New Components Created

#### Security Components

**PkiCertificateValidator.java**
- Validates X.509 client certificates
- Extracts certificate attributes (CN, O, C, Serial Number, etc.)
- Performs certificate expiration checking
- Provides certificate information for logging and auditing

**PkiAuthenticationFilter.java**
- Extracts X.509 client certificates from TLS handshake
- Validates certificate validity
- Creates Spring Security authentication tokens
- Grants `ROLE_PKI_USER` authority
- Extracts and stores certificate details in request attributes

#### Configuration Components

**PkiSecurityConfig.java**
- Conditional configuration (`@ConditionalOnProperty pki.enabled=true`)
- Loads server keystore for SSL/TLS
- Loads truststore for client certificate validation
- Initializes SSLContext with KeyManagerFactory and TrustManagerFactory
- Creates and registers PKI authentication filter bean

**Updated SecurityConfig.java**
- Dual authentication filter chain support
- Authorization matchers for different endpoints:
  - Public endpoints: `/api/token`, Swagger UI, Actuator
  - PKI endpoints: `/api/pki/**` (requires `ROLE_PKI_USER`)
  - OAuth2 endpoints: `/api/oauth2/**` (requires JWT token)
  - General API: Supports both authentication methods
- Stateless session management
- CSRF protection disabled for API

#### API Controller

**PkiController.java**
- `/api/pki/health` - PKI health check and certificate verification
- `/api/pki/certificate-info` - Detailed certificate information
- `/api/pki/verify` - Certificate validity verification
- `/api/pki/secure-data` - Protected resource accessible only via PKI
- `/api/pki/auth-method` - Determine which authentication method was used

### 2. Configuration Updates

**application.yml**
```yaml
server:
  ssl:
    enabled: false  # Set to true to enable HTTPS
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    client-auth: want  # Optional client certs

pki:
  enabled: false  # Set to true to enable PKI endpoints
  truststore-path: classpath:truststore.p12
  truststore-password: changeit
  certificate:
    validate-expiry: true
    validate-chain: true
```

### 3. Certificate Management

**scripts/generate-certificates.sh** (Fixed)
- Generates Root CA certificate and key
- Generates server certificate and key
- Generates client certificate and key
- Creates PKCS12 keystores for both server and client
- Creates truststore with CA certificate
- Verifies all certificates
- Sets proper file permissions

**Files Generated:**
- `certs/ca-cert.pem` - Root CA certificate (public, safe to commit)
- `certs/ca-key.pem` - Root CA private key (KEEP SECURE)
- `certs/server-cert.pem` - Server certificate
- `certs/server-key.pem` - Server private key
- `certs/client-cert.pem` - Client certificate for testing
- `certs/client-key.pem` - Client private key for testing
- `certs/keystore.p12` - Server keystore (password: changeit)
- `certs/truststore.p12` - Server truststore (password: changeit)
- `certs/client-keystore.p12` - Client keystore for testing

---

## File Structure

```
src/main/java/com/judy/secureprojecthub/
├── config/
│   ├── JwtConfig.java                 # JWT configuration (existing)
│   ├── SecurityConfig.java            # UPDATED - dual auth support
│   ├── PkiSecurityConfig.java         # NEW - PKI/mTLS configuration
│   └── [other configs...]
├── security/
│   ├── JwtTokenFilter.java            # JWT filter (existing)
│   ├── PkiCertificateValidator.java   # NEW - certificate validation
│   ├── PkiAuthenticationFilter.java   # NEW - PKI filter
│   └── [other security...]
└── controller/
    ├── AuthController.java             # Token generation (existing)
    ├── PkiController.java              # NEW - PKI endpoints
    └── [other controllers...]

src/main/resources/
├── application.yml                     # UPDATED - PKI config
├── db/migration/                       # Database migrations
└── (keystore.p12, truststore.p12)      # To be copied after cert generation

docs/
├── DUAL_AUTHENTICATION_SETUP.md        # NEW - Complete setup guide
└── [other documentation...]

scripts/
└── generate-certificates.sh            # FIXED - certificate generation
```

---

## How It Works

### OAuth2 JWT Authentication Flow

1. Client sends credentials to `POST /api/token`
2. Server validates credentials against database
3. Server generates JWT token with user claims
4. Client receives token and stores it
5. Client sends token in `Authorization: Bearer <token>` header
6. `JwtTokenFilter` validates token signature and expiration
7. Request is authenticated with user roles from database

### PKI/mTLS Authentication Flow

1. Client initiates TLS connection with client certificate
2. Server performs TLS handshake and validates certificate against truststore
3. `PkiAuthenticationFilter` extracts certificate from request
4. Certificate attributes (CN, O, C) are extracted and validated
5. `ROLE_PKI_USER` authority is granted to authenticated request
6. Certificate details are stored in request attributes
7. Request is authenticated based on certificate

### Endpoint Routing

```
Public Endpoints (No Auth):
├── POST /api/token (JWT token generation)
├── GET /swagger-ui/** (API documentation)
└── GET /v3/api-docs/** (OpenAPI specification)

PKI-Only Endpoints (Client Certificate Required):
├── GET /api/pki/health
├── GET /api/pki/certificate-info
├── GET /api/pki/verify
├── GET /api/pki/secure-data
└── GET /api/pki/auth-method

OAuth2-Only Endpoints (JWT Token Required):
├── GET /api/oauth2/** (if implemented)

Dual-Auth Endpoints (Either OAuth2 or PKI):
├── GET /api/users
├── GET /api/roles
└── [other API endpoints]
```

---

## Quick Start

### 1. Enable PKI/mTLS

Edit `src/main/resources/application.yml`:
```yaml
server:
  ssl:
    enabled: true

pki:
  enabled: true
```

### 2. Generate Certificates

```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
bash scripts/generate-certificates.sh
```

### 3. Copy Keystores

```bash
cp certs/keystore.p12 src/main/resources/
cp certs/truststore.p12 src/main/resources/
```

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

### 5. Test PKI Endpoints

```bash
# PKI health check
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

### 6. Test OAuth2 Endpoints

```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')

# Use token
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/users
```

---

## Security Considerations

### Client Authentication Modes

- **want** (default): Client certificate is optional, connections allowed without it
- **need**: Client certificate is required, connections without it are rejected
- **none**: No client certificate validation (plain TLS)

For development use `want`, for production consider `need` if all clients support PKI.

### Certificate Security

- **Private Keys**: Never commit to Git, use environment variables in production
- **CA Key**: Keep in secure vault, never distribute
- **Public Certificates**: Safe to commit and distribute
- **Expiration**: Monitor and renew certificates before expiration
- **Revocation**: Use CRL or OCSP for certificate revocation checks

### Authorization

- PKI users get `ROLE_PKI_USER` authority
- Organization field in certificate can be used for tenant isolation
- Certificate serial number can be used for user identification
- Common Name (CN) can be used as username equivalent

---

## Testing Endpoints

### PKI Endpoints (Requires Client Certificate)

```bash
# 1. Health Check
curl --cert certs/client-cert.pem --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health

# 2. Certificate Information
curl --cert certs/client-cert.pem --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info

# 3. Certificate Verification
curl --cert certs/client-cert.pem --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify

# 4. Secure Data (PKI Only)
curl --cert certs/client-cert.pem --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data

# 5. Authentication Method
curl --cert certs/client-cert.pem --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method
```

### OAuth2 Endpoints (Requires JWT Token)

```bash
# 1. Get Token
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')

# 2. Use Token
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/users

# 3. Authentication Method
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/pki/auth-method
```

---

## Build Status

✅ **All files compile successfully**

```
[INFO] BUILD SUCCESS
[INFO] Total time: 1.334 s
[INFO] Finished at: 2026-04-13T11:28:14-04:00
```

---

## Documentation

Complete setup and testing guide available in:
- `docs/DUAL_AUTHENTICATION_SETUP.md` - Detailed configuration and troubleshooting

---

## Next Steps (Optional)

1. **Database User Mapping**: Map certificate CNs to database users
2. **Audit Logging**: Log all PKI authentication attempts
3. **Certificate Revocation**: Implement CRL or OCSP checking
4. **Role Mapping**: Map certificate organization to roles
5. **UI Integration**: Update frontend to support mTLS if needed
6. **Performance**: Monitor and optimize TLS handshake performance
7. **Monitoring**: Set up alerts for certificate expiration
8. **Load Balancing**: Configure load balancers to pass client certificates

---

## Related Documentation

- `docs/PKI_vs_OAUTH2.md` - Comparison of authentication methods
- `docs/AUTHORIZATION.md` - Authorization configuration
- `README.md` - Project overview
- `HELP.md` - General help

---

**Implementation Date:** April 13, 2026  
**Status:** ✅ Complete and Tested  
**Build Status:** ✅ Success
