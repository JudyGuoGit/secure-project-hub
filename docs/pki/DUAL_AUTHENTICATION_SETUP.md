# Dual Authentication: OAuth2 JWT + PKI/mTLS

This document explains how to use both OAuth2 JWT and PKI/mTLS authentication in the Secure Project Hub application.

## Overview

The Secure Project Hub now supports **two independent authentication methods**:

1. **OAuth2 JWT** (Token-based) - For user-facing applications, mobile apps, third-party integrations
2. **PKI/mTLS** (Certificate-based) - For service-to-service communication, highly secure internal systems, compliance requirements

Both authentication methods work simultaneously without interfering with each other.

## Architecture

### Authentication Flow

```
OAuth2 JWT Flow:
1. Client sends username/password to /api/token
2. Server issues JWT token
3. Client includes token in Authorization: Bearer <token> header
4. JwtTokenFilter validates and authenticates the request

PKI/mTLS Flow:
1. Client initiates TLS handshake with client certificate
2. Server validates certificate against truststore
3. PkiAuthenticationFilter extracts certificate details
4. Certificate details are used as authentication
```

### File Structure

```
src/main/java/com/judy/secureprojecthub/
├── config/
│   ├── PkiSecurityConfig.java        # PKI/mTLS configuration
│   ├── SecurityConfig.java            # Updated for dual auth
│   └── JwtConfig.java                 # JWT configuration
├── security/
│   ├── PkiCertificateValidator.java   # Certificate validation logic
│   ├── PkiAuthenticationFilter.java   # PKI authentication filter
│   └── JwtTokenFilter.java            # JWT authentication filter
└── controller/
    └── PkiController.java             # PKI endpoints
```

## Configuration

### Enable PKI/mTLS

Edit `src/main/resources/application.yml`:

```yaml
server:
  ssl:
    enabled: true  # Enable SSL/TLS
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: localhost
    client-auth: want  # 'want' = optional, 'need' = required

pki:
  enabled: true  # Enable PKI endpoints
  truststore-path: classpath:truststore.p12
  truststore-password: changeit
  truststore-type: PKCS12
  certificate:
    subject-attribute: CN
    issuer-attribute: O
    validate-expiry: true
    validate-chain: true
```

### Client Auth Modes

- `none` - No client certificate (plain TLS)
- `want` - Client certificate optional
- `need` - Client certificate required

## Certificates Setup

### Generate Certificates

Run the certificate generation script:

```bash
cd /Users/jguo/work/eclipse-workspace/secure-project-hub
bash scripts/generate-certificates.sh
```

This creates certificates in `./certs/`:

```
certs/
├── ca-key.pem              # Root CA private key (keep secure!)
├── ca-cert.pem             # Root CA certificate
├── server-key.pem          # Server private key
├── server-cert.pem         # Server certificate
├── client-key.pem          # Client private key
├── client-cert.pem         # Client certificate
├── keystore.p12            # Server keystore
├── truststore.p12          # Truststore with CA
└── client-keystore.p12     # Client keystore for testing
```

### Deploy Certificates

Copy keystores to resources:

```bash
cp certs/keystore.p12 src/main/resources/
cp certs/truststore.p12 src/main/resources/
cp certs/ca-cert.pem src/main/resources/
```

## API Endpoints

### OAuth2 Endpoints (JWT Authentication Required)

**Get Token:**
```bash
curl -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'
```

**Access Protected Resource:**
```bash
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/users
```

### PKI Endpoints (Client Certificate Required)

All PKI endpoints are under `/api/pki/*` and require a valid client certificate.

**PKI Health Check:**
```bash
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/health
```

**Get Certificate Info:**
```bash
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/certificate-info
```

**Verify Certificate:**
```bash
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/verify
```

**Access Secure Data (PKI Only):**
```bash
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/secure-data
```

**Get Authentication Method:**
```bash
# With PKI:
curl --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     --cacert certs/ca-cert.pem \
     https://localhost:8443/api/pki/auth-method

# With OAuth2:
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/pki/auth-method
```

### Public Endpoints (No Authentication Required)

```bash
# Token endpoint
curl -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}'

# Swagger UI
http://localhost:8080/swagger-ui/index.html

# API Documentation
http://localhost:8080/v3/api-docs
```

## Security Configuration

### Authorization Rules

**Public (No Auth):**
- `POST /api/token` - JWT token generation
- `GET /swagger-ui/**` - API documentation
- `GET /v3/api-docs/**` - OpenAPI spec
- `GET /actuator/**` - Health checks

**PKI Only (`/api/pki/**`):**
- Requires `ROLE_PKI_USER` authority
- Can only be accessed with valid client certificate

**OAuth2/JWT (`/api/oauth2/**`):**
- Requires `ROLE_USER` or `ROLE_ADMIN` authority
- Accessed with Bearer token

**General API (`/api/**`):**
- Requires authentication (OAuth2 or PKI)
- Role-based access control

## Implementation Details

### Certificate Validation (PkiCertificateValidator.java)

The validator performs:

1. **Certificate Format Validation** - Ensures X.509 format
2. **Expiration Checking** - Validates `notBefore` and `notAfter` dates
3. **DN Extraction** - Parses Common Name, Organization, Country, etc.
4. **Chain Validation** - Verifies certificate chain (when enabled)

### Authentication Filter (PkiAuthenticationFilter.java)

The filter:

1. Extracts client certificate from request
2. Validates certificate expiration and format
3. Extracts certificate attributes (CN, O, C)
4. Creates Spring Security authentication token
5. Sets `ROLE_PKI_USER` authority
6. Stores certificate in request attributes for later use

### Security Configuration (SecurityConfig.java)

Configured with:

1. Dual filter chains:
   - JWT filter for OAuth2 endpoints
   - PKI filter for mTLS endpoints
2. Authorization matchers for different endpoint groups
3. Stateless session management
4. CSRF protection disabled (for API)

## Testing

### Manual Testing with curl

**Test PKI Authentication:**
```bash
# Health check (should return 200 with certificate info)
curl -v --cert certs/client-cert.pem \
        --key certs/client-key.pem \
        --cacert certs/ca-cert.pem \
        https://localhost:8443/api/pki/health
```

**Test Certificate Without Valid Client Cert:**
```bash
# Should fail (403 Forbidden or 401 Unauthorized)
curl -v --cacert certs/ca-cert.pem \
        https://localhost:8443/api/pki/health
```

**Test OAuth2 Authentication:**
```bash
# Get token
TOKEN=$(curl -s -X POST http://localhost:8080/api/token \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password"}' | jq -r '.token')

# Use token to access protected endpoint
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/users
```

### Automated Testing

Create integration tests in `src/test/java/`:

```java
@SpringBootTest
@AutoConfigureMockMvc
class PkiAuthenticationTests {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testPkiEndpointRequiresCertificate() throws Exception {
        mockMvc.perform(get("/api/pki/health"))
            .andExpect(status().isUnauthorized());
    }
}
```

## Troubleshooting

### Certificate Validation Fails

**Error:** `certificate verify failed`

**Solution:**
- Ensure truststore.p12 is in `src/main/resources/`
- Verify certificate is signed by the CA in truststore
- Check certificate expiration: `openssl x509 -in cert.pem -text -noout | grep -A 2 Validity`

### PKIX Path Building Failed

**Error:** `unable to find valid certification path to requested target`

**Solution:**
- CA certificate in truststore must match the CA that signed the client certificate
- Regenerate certificates using the provided script

### SSL Handshake Fails

**Error:** `SSLHandshakeException: Unexpected handshake message`

**Solution:**
- Ensure `server.ssl.client-auth` is set to `want` or `need`
- Check that keystore.p12 is correctly formatted
- Verify server certificate is properly signed

### PKI Endpoints Return 403

**Error:** `Access Denied`

**Solution:**
- Ensure `pki.enabled: true` in application.yml
- Verify client certificate has `ROLE_PKI_USER` authority
- Check that `PkiAuthenticationFilter` is registered in Spring Security chain

## Certificate Management

### Adding New Client Certificates

To add a new client for PKI authentication:

```bash
cd certs

# Generate new client private key
openssl genrsa -out new-client-key.pem 2048

# Generate CSR
openssl req -new -key new-client-key.pem -out new-client.csr \
  -subj "/CN=new-client/O=Secure-Project-Hub/C=US"

# Sign with CA
openssl x509 -req -in new-client.csr \
  -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out new-client-cert.pem -days 365 -sha256

# Create keystore
openssl pkcs12 -export -in new-client-cert.pem -inkey new-client-key.pem \
  -out new-client-keystore.p12 -name new-client -passout pass:changeit
```

### Certificate Renewal

Certificates expire after the validity period. To renew:

1. Regenerate certificates using `scripts/generate-certificates.sh`
2. Update keystores in `src/main/resources/`
3. Restart the application

## Production Considerations

### Security Best Practices

1. **Store Private Keys Securely:**
   - Never commit private keys to Git
   - Use .gitignore to exclude keystore files
   - Use secure key management service in production

2. **Certificate Validation:**
   - Enable `validate-expiry: true` in production
   - Enable `validate-chain: true` for full chain validation
   - Use CRL or OCSP for revocation checking

3. **SSL/TLS Settings:**
   - Use strong cipher suites (TLS 1.2+)
   - Disable weak protocols
   - Use HTTPS (not HTTP) in production

4. **Access Control:**
   - Map certificate attributes to user roles
   - Use organization (O) field for tenant isolation
   - Implement audit logging for PKI access

### Configuration for Production

```yaml
server:
  ssl:
    enabled: true
    key-store: ${KEYSTORE_PATH}  # Set via environment variable
    key-store-password: ${KEYSTORE_PASSWORD}
    client-auth: need  # Require client certificate

pki:
  enabled: true
  truststore-path: ${TRUSTSTORE_PATH}
  truststore-password: ${TRUSTSTORE_PASSWORD}
  certificate:
    validate-expiry: true
    validate-chain: true
```

## Reference

- [X.509 Certificates](https://en.wikipedia.org/wiki/X.509)
- [mTLS (Mutual TLS)](https://en.wikipedia.org/wiki/Mutual_authentication)
- [OAuth2 vs mTLS](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-mtls)
- [Spring Security SSL/TLS](https://spring.io/guides/gs/securing-web/)
- [OpenSSL Commands](https://www.openssl.org/docs/)
