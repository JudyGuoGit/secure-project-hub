# Certificate Distribution Guide - Real World Deployment

## Development vs Production

### ✅ Development (Current Setup)
Currently, all certificate files are in one `certs/` directory for testing purposes. This is fine for local development where you're testing both server and client.

### 🚀 Production/Real-World Setup

```
Server Application (secure-project-hub)
├── src/main/resources/
│   ├── keystore.p12              ← Server's private keystore
│   └── truststore.p12            ← Server's truststore (public CAs)
├── certs/
│   ├── ca-cert.pem               ← For reference/documentation
│   └── server-cert.pem           ← For reference/documentation
└── .gitignore
    ├── keystore.p12              ← Don't commit
    ├── ca-key.pem                ← Don't commit
    └── server-key.pem            ← Don't commit

Client Application (separate repo/project)
├── certs/
│   ├── ca-cert.pem               ← CA certificate from server
│   ├── client-cert.pem           ← This client's certificate
│   ├── client-key.pem            ← This client's private key
│   └── client-keystore.p12       ← Optional: binary format
├── config/
│   └── client-mtls-config.yml    ← mTLS configuration
└── .gitignore
    ├── client-key.pem            ← Don't commit
    └── client-keystore.p12       ← Don't commit
```

---

## File Distribution Matrix

### What Server Needs

```
✅ keystore.p12 (contains server-cert.pem + server-key.pem)
   └─ Located: src/main/resources/
   └─ Contains: Server's certificate + private key
   └─ Used for: HTTPS/mTLS server authentication

✅ truststore.p12 (contains ca-cert.pem)
   └─ Located: src/main/resources/
   └─ Contains: Root CA certificate
   └─ Used for: Validating client certificates
```

### What Client Needs

```
✅ ca-cert.pem
   └─ Obtained from: Server admin
   └─ Contains: Root CA public certificate
   └─ Used for: Validating server certificate

✅ client-cert.pem
   └─ Obtained from: Server admin (certificate signed by CA)
   └─ Contains: Client's public certificate
   └─ Used for: Authenticating to server

✅ client-key.pem
   └─ Obtained from: Server admin (generated for this client only)
   └─ Contains: Client's private key
   └─ Used for: Proving client identity
   └─ SECURITY: Each client gets unique key pair
```

### What Should NOT Be Distributed

```
❌ ca-key.pem
   └─ Reason: Root CA private key (critical!)
   └─ Keep: In secure vault only
   └─ Who: Only CA administrator

❌ server-key.pem
   └─ Reason: Server private key
   └─ Keep: In secure vault only
   └─ Who: Only server administrator

❌ keystore.p12 (server's)
   └─ Reason: Contains server-key.pem
   └─ Keep: In secure vault only
   └─ Who: Only server administrator
```

---

## Real-World Workflow

### Step 1: Server Admin - Generate CA and Certificates

```bash
# Server admin runs this once (in secure vault)
bash scripts/generate-certificates.sh

# Results:
# ✅ ca-cert.pem       → Share with clients
# ✅ ca-key.pem        → Keep in vault (never distribute)
# ✅ keystore.p12      → Keep on server
# ✅ truststore.p12    → Can be public (contains only ca-cert.pem)
```

### Step 2: Server Admin - Provide Client Certificates

For each client that needs to connect:

```bash
# Generate unique client certificate
cd certs
openssl genrsa -out client-name-key.pem 2048
openssl req -new -key client-name-key.pem -out client-name.csr \
  -subj "/CN=client-name/O=YourOrg/C=US"
openssl x509 -req -in client-name.csr -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out client-name-cert.pem -days 365 -sha256

# Package for client
mkdir -p client-name-certs
cp ca-cert.pem client-name-certs/
cp client-name-cert.pem client-name-certs/
cp client-name-key.pem client-name-certs/

# Send to client (via secure channel, not email!)
tar czf client-name-certs.tar.gz client-name-certs/
# Securely transmit to client
```

### Step 3: Client Application - Use Certificates

Client application structure:

```
client-app/
├── src/
│   └── main/
│       └── resources/
│           └── certs/
│               ├── ca-cert.pem          ← From server
│               ├── client-cert.pem      ← From server
│               └── client-key.pem       ← From server
├── application.yml
└── pom.xml (or gradle config)
```

Client configuration (`application.yml`):

```yaml
server:
  ssl:
    # Client uses keystore if needed
    key-store: classpath:certs/client-keystore.p12
    key-store-password: ${CLIENT_KEYSTORE_PASSWORD}

# Or use individual files
client:
  ssl:
    certificate-path: classpath:certs/client-cert.pem
    key-path: classpath:certs/client-key.pem
    ca-certificate-path: classpath:certs/ca-cert.pem
```

### Step 4: Client Application - Connect to Server

```java
// Example: Java HTTP client with mTLS
HttpClient client = HttpClient.newBuilder()
    .sslContext(createSSLContext(
        "certs/client-cert.pem",
        "certs/client-key.pem",
        "certs/ca-cert.pem"
    ))
    .build();

// Make request
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://server.example.com:8443/api/pki/health"))
    .GET()
    .build();

HttpResponse<String> response = client.send(request, 
    HttpResponse.BodyHandlers.ofString());
```

---

## Current Project Structure (For Reference)

```
secure-project-hub/
├── certs/                          ← Development/testing only
│   ├── ca-cert.pem                 ✅ Public
│   ├── ca-key.pem                  ❌ Private (in vault)
│   ├── server-cert.pem             ✅ Public (reference)
│   ├── server-key.pem              ❌ Private (in vault)
│   ├── client-cert.pem             ✅ For testing
│   ├── client-key.pem              ❌ For testing only
│   ├── keystore.p12                ❌ Private (on server)
│   ├── truststore.p12              ✅ Public
│   └── client-keystore.p12         ✅ For testing
│
├── src/main/resources/
│   ├── keystore.p12                ← Server uses this (DO NOT COMMIT)
│   └── truststore.p12              ← Server uses this
│
└── docs/
    └── CERTIFICATE_DISTRIBUTION.md ← This file
```

---

## Git Configuration

### Server App `.gitignore`

```gitignore
# Certificate files - NEVER commit private keys
ca-key.pem
server-key.pem
client-key.pem
*.srl

# Keystores (contain private keys)
keystore.p12
client-keystore.p12

# Local cert directory (for development only)
/certs/

# But DO commit truststore if it's public
!truststore.p12
```

### Client App `.gitignore`

```gitignore
# Certificate files - NEVER commit private keys
client-key.pem
ca-key.pem

# Keystores (contain private keys)
client-keystore.p12

# Environment secrets
.env
*.local
```

---

## Security Best Practices

### 🔐 Private Keys
- Each client gets a **unique private key**
- Never share private keys over insecure channels
- Store in secure vault (HashiCorp Vault, AWS Secrets Manager, etc.)
- Rotate periodically (e.g., every 90 days)

### 🔒 Certificate Distribution
- **Secure channels only:**
  - SFTP/SCP
  - Encrypted email with GPG
  - VPN + secure file transfer
  - Company internal secure portal
- **Never:**
  - Plain HTTP
  - Email without encryption
  - Public repositories
  - Shared drives without encryption

### 📋 Certificate Management
- **Tracking:** Maintain a registry of issued certificates
  - Client name
  - Certificate serial number
  - Issue date
  - Expiration date
  - Last rotation date

- **Rotation:** Plan certificate renewal before expiration
  - Set expiration date: 365 days
  - Renew at: 345 days (20 days before expiration)
  - Alert: 30 days before expiration

- **Revocation:** Implement CRL (Certificate Revocation List) if needed
  - For compromised keys
  - For decommissioned clients
  - For security incidents

---

## Production Deployment Checklist

### Server Setup
- [ ] Generate CA and server certificates in secure vault
- [ ] Store `ca-key.pem` in vault (e.g., HashiCorp Vault)
- [ ] Store `server-key.pem` in vault
- [ ] Deploy only `keystore.p12` and `truststore.p12` to server
- [ ] Set environment variables for keystore passwords
- [ ] Enable firewall rules for HTTPS (port 8443)
- [ ] Set `client-auth: need` in production (requires client cert)
- [ ] Enable audit logging for all mTLS connections

### Client Setup
- [ ] Request certificates from server admin
- [ ] Receive certificates via secure channel
- [ ] Verify certificate chain: `openssl verify`
- [ ] Store `client-key.pem` securely
- [ ] Implement certificate rotation logic
- [ ] Set alerts for certificate expiration
- [ ] Configure retry logic for failed connections
- [ ] Test connection before going to production

---

## Quick Reference: What Goes Where

```
┌─────────────────────────────────────────────────────────────┐
│                      DEVELOPMENT                            │
├─────────────────────────────────────────────────────────────┤
│ All files in one certs/ directory for easy testing          │
│ - Server reads: keystore.p12, truststore.p12               │
│ - Client reads: client-cert.pem, client-key.pem, ca-cert  │
│ - Both in same repo for convenience                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                      PRODUCTION                             │
├─────────────────────────────────────────────────────────────┤
│ Server:                                                      │
│ ├─ keystore.p12 (from vault)                               │
│ └─ truststore.p12 (from vault)                             │
│                                                              │
│ Client (different repo/organization):                       │
│ ├─ client-cert.pem (received from server)                  │
│ ├─ client-key.pem (received securely, stored locally)      │
│ └─ ca-cert.pem (received from server)                      │
│                                                              │
│ Vault (internal only, never version controlled):           │
│ ├─ ca-key.pem                                              │
│ ├─ server-key.pem                                          │
│ ├─ All previous versions of certificates                   │
│ └─ Rotation schedule                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Summary

| Aspect | Development | Production |
|--------|-------------|------------|
| Certificate Location | One `certs/` directory | Vault + distributed to clients |
| Client Files | In same repo | In separate client app repo |
| Private Keys | In certs/ folder | In secure vault only |
| Distribution | Via Git (local dev) | Via secure channels only |
| Who Accesses | Developers | Server admin + authorized clients |
| File Permissions | 600 (secure) | 400 (restricted) + vault encryption |

Your instinct is correct! In production, client files absolutely belong in the client application, not on the server. This project currently has them together for development/testing convenience. 🎯
