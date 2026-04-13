# Secure Project Hub - Dual Authentication Architecture

## System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Secure Project Hub Application                      │
│                      (Spring Boot 4.0.5 + Java 21)                          │
└─────────────────────────────────────────────────────────────────────────────┘

                              HTTP/HTTPS Requests
                                      │
                    ┌───────────────────┼───────────────────┐
                    │                   │                   │
                    ▼                   ▼                   ▼
            ┌──────────────┐    ┌──────────────┐   ┌──────────────┐
            │   OAuth2     │    │    PKI/mTLS  │   │    Public    │
            │   (HTTP)     │    │   (HTTPS)    │   │  Endpoints   │
            └──────────────┘    └──────────────┘   └──────────────┘
                    │                   │                   │
         ┌──────────▼─────────┐        │         ┌─────────▼──────────┐
         │  Bearer Token      │        │         │  No Auth Required  │
         │  Validation        │        │         │                    │
         │                    │        │         │  /api/token        │
         │  JwtTokenFilter    │        │         │  /swagger-ui       │
         │  ├─ Extract Token  │        │         │  /v3/api-docs      │
         │  ├─ Validate Sig   │        │         │  /actuator         │
         │  └─ Load Roles     │        │         └────────────────────┘
         │     from DB        │        │
         └──────────┬─────────┘        │
                    │                  │
                    │         ┌────────▼──────────┐
                    │         │  TLS Handshake    │
                    │         │                   │
                    │         │  Client sends     │
                    │         │  certificate      │
                    │         └────────┬──────────┘
                    │                  │
                    │         ┌────────▼──────────────────┐
                    │         │ PkiAuthenticationFilter   │
                    │         │ ├─ Extract Certificate    │
                    │         │ ├─ Validate Expiry        │
                    │         │ ├─ Extract Attributes     │
                    │         │ │  (CN, O, C)             │
                    │         │ └─ Create Auth Token      │
                    │         │    ROLE_PKI_USER          │
                    │         └────────┬──────────────────┘
                    │                  │
         ┌──────────▼──────────┐      │      ┌──────────────────┐
         │ SecurityFilterChain │      │      │ PkiSecurityConfig │
         │                    │      │      │                   │
         │ ┌────────────────┐ │      │      │ ├─ SSLContext    │
         │ │ Authorization  │ │      │      │ ├─ KeyStore      │
         │ │                │ │      │      │ └─ TrustStore    │
         │ │ OAuth2: User   │ │      │      └──────────────────┘
         │ │ Role from JWT  │ │      │
         │ │                │ │      └──────────┐
         │ │ PKI: Role from │ │                 │
         │ │ Certificate    │ │                 │
         │ └────────────────┘ │                 │
         └──────────┬──────────┘                 │
                    │                           │
         ┌──────────▼──────────────────────────▼──────┐
         │        Controller Layer                    │
         │                                             │
         │ ┌─────────────┐    ┌────────────────┐     │
         │ │  Auth       │    │  Pki           │     │
         │ │  Controller │    │  Controller    │     │
         │ │             │    │                │     │
         │ │ /api/token  │    │ /api/pki/...   │     │
         │ └─────────────┘    └────────────────┘     │
         │                                             │
         │ ┌─────────────────────────────────────┐   │
         │ │  User/Role/Permission Controllers   │   │
         │ │  (Accessible via either auth)       │   │
         │ │  /api/users, /api/roles, etc.       │   │
         │ └─────────────────────────────────────┘   │
         └─────────────────┬──────────────────────────┘
                           │
         ┌─────────────────▼──────────────────┐
         │      Service Layer                 │
         │                                     │
         │ ├─ UserService                     │
         │ ├─ RoleService                     │
         │ ├─ PermissionService               │
         │ └─ PkiCertificateValidator         │
         └─────────────────┬──────────────────┘
                           │
         ┌─────────────────▼──────────────────┐
         │    Repository Layer                │
         │    (Spring Data JPA)                │
         │                                     │
         │ ├─ UserRepository                  │
         │ ├─ RoleRepository                  │
         │ ├─ PermissionRepository            │
         │ └─ AuditLogRepository              │
         └─────────────────┬──────────────────┘
                           │
         ┌─────────────────▼──────────────────┐
         │    PostgreSQL Database             │
         │                                     │
         │ ├─ users (auth user DB)            │
         │ ├─ roles                           │
         │ ├─ permissions                     │
         │ ├─ user_roles (mapping)            │
         │ ├─ role_permissions (mapping)      │
         │ └─ audit_logs                      │
         └─────────────────────────────────────┘
```

---

## Authentication Flows

### OAuth2 JWT Flow (Stateless Token-Based)

```
┌─────────────────────────────────────────────────────────────────────┐
│                      OAuth2 JWT Authentication Flow                 │
└─────────────────────────────────────────────────────────────────────┘

User/Client                          Server                     Database
    │                                  │                            │
    ├──1. Login (username/password)──▶ │                            │
    │                                  │                            │
    │                                  ├──2. Verify Credentials───▶ │
    │                                  │                            │
    │                                  │◀──3. User Data + Roles ─── │
    │                                  │                            │
    │                                  ├──4. Generate JWT Token     │
    │                                  │    (signed with secret)    │
    │                                  │                            │
    │◀──5. JWT Token ─────────────────┤                            │
    │     (access_token, expires_in)   │                            │
    │                                  │                            │
    ├──6. API Request + Bearer Token──▶ │                            │
    │     (Authorization header)        │                            │
    │                                  │                            │
    │                                  ├──7. JwtTokenFilter         │
    │                                  │    ├─ Extract Token        │
    │                                  │    ├─ Verify Signature     │
    │                                  │    └─ Load Roles/Perms     │
    │                                  │                            │
    │                                  ├──8. Authorize Request      │
    │                                  │                            │
    │                                  ├──9. Process Request        │
    │                                  │                            │
    │◀───10. Response (with data) ────┤                            │
    │                                  │                            │
    
Repeat steps 6-10 for each API call
Token expires after set duration (typically 1 hour)
```

### PKI/mTLS Flow (Certificate-Based)

```
┌─────────────────────────────────────────────────────────────────────┐
│                    PKI/mTLS Authentication Flow                      │
└─────────────────────────────────────────────────────────────────────┘

Client (with certificate)            Server (with CA cert)
    │                                      │
    ├──1. TLS ClientHello ────────────────▶│
    │    (includes supported ciphers)      │
    │                                      │
    │◀─────2. ServerHello ────────────────┤
    │         (chooses cipher suite)       │
    │                                      │
    │◀─────3. Server Certificate ────────┤
    │         (sent by server)             │
    │                                      │
    │◀─────4. CertificateRequest ────────┤
    │         (server asks for client cert)│
    │                                      │
    │                                      │
    ├──5. ClientCertificate ────────────▶│
    │     (client sends certificate)       │
    │                                      │
    │                                      ├──6. Verify Certificate
    │                                      │    ├─ Valid Format?
    │                                      │    ├─ Not Expired?
    │                                      │    ├─ Signed by CA?
    │                                      │    └─ In Truststore?
    │                                      │
    │                                      ├──7. Extract Cert Data
    │                                      │    ├─ Common Name (CN)
    │                                      │    ├─ Organization (O)
    │                                      │    ├─ Country (C)
    │                                      │    └─ Serial Number
    │                                      │
    │◀─────8. ServerHelloDone ──────────┤
    │         (ready for secure channel)   │
    │                                      │
    │◀─────9. ChangeCipherSpec ─────────┤
    │         & Finished                   │
    │                                      │
    ├──10. ChangeCipherSpec ────────────▶│
    │      & Finished                      │
    │                                      │
    │      ✅ TLS CHANNEL ESTABLISHED     │
    │         (encrypted & authenticated)  │
    │                                      │
    ├──11. HTTPS Request ───────────────▶│
    │      (encrypted with cert attributes)
    │                                      │
    │                                      ├──12. PkiAuthenticationFilter
    │                                      │    ├─ Create Auth Token
    │                                      │    ├─ Set ROLE_PKI_USER
    │                                      │    └─ Store Cert Info
    │                                      │
    │                                      ├──13. Authorize & Process
    │                                      │
    │◀─12. HTTPS Response ──────────────┤
    │      (encrypted)                     │
    │                                      │

Subsequent requests reuse same encrypted channel (no re-handshake needed)
Certificate expires after validity period (typically 365 days)
```

---

## Component Interaction Diagram

```
┌────────────────────────────────────────────────────────────────────┐
│                          HTTP Request                              │
└──────────────────────────────────┬─────────────────────────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │  Spring Security           │
                    │  FilterChain               │
                    └──────────────┬──────────────┘
                                   │
                ┌──────────────────┼──────────────────┐
                │                  │                  │
                ▼                  ▼                  ▼
        ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
        │ Cors Filter  │   │ JwtToken     │   │ Pki Auth     │
        │              │   │ Filter       │   │ Filter       │
        │ (public      │   │              │   │              │
        │  endpoints)  │   │ (extracts    │   │ (extracts    │
        │              │   │  jwt token)  │   │  certificate)│
        └──────┬───────┘   └──────┬───────┘   └──────┬───────┘
               │                  │                  │
               └──────────────────┼──────────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │  Authorization Check      │
                    │  @PreAuthorize            │
                    │  hasRole(...) ?           │
                    └─────────────┬──────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │  Controller Handler       │
                    │  Method Invocation        │
                    └─────────────┬──────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │  Service Layer            │
                    │  Business Logic           │
                    └─────────────┬──────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │  Repository Layer         │
                    │  Database Access          │
                    └─────────────┬──────────────┘
                                  │
                    ┌─────────────▼──────────────┐
                    │  Response Object          │
                    │  (JSON)                   │
                    └──────────────────────────────┘
```

---

## Security Components Relationship

```
┌─────────────────────────────────────────────────────────────┐
│                   Secure Project Hub                        │
│                 Security Architecture                       │
└─────────────────────────────────────────────────────────────┘

                    ┌──────────────────────┐
                    │   SecurityConfig     │
                    │   @Configuration     │
                    └──────────┬───────────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
                ▼              ▼              ▼
        ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
        │ JwtTokenFilter   │ Pki          │ PermitAll     │
        │                  │ AuthFilter   │ Handler       │
        │ ├─ Extract JWT   │              │ ├─ /api/token │
        │ ├─ Verify Sig    │ ├─ Extract  │ ├─ /swagger-ui│
        │ └─ Load Roles    │ │  Cert     │ └─ /api-docs  │
        │   from DB        │ ├─ Validate │               │
        │                  │ │  Expiry   │               │
        │                  │ └─ Create   │               │
        │                  │    Auth     │               │
        └──────────┬───────┘  └─────┬─────┘  └─────┬──────┘
                   │                │              │
                ┌──▼────────────────▼──────────────▼──┐
                │   Authorization Check Layer        │
                │   (RequestMatcher.matches)         │
                └──┬─────────────────────────────────┘
                   │
         ┌─────────┼─────────┐
         │         │         │
         ▼         ▼         ▼
    ┌────────┐ ┌────────┐ ┌────────┐
    │ OAuth2 │ │  PKI   │ │ Other  │
    │ Endpoints │ Endpoints │ API    │
    │/oauth2/ │ /pki/  │ /api/   │
    │         │        │        │
    │ Require │ Require │ Require │
    │ JWT     │  Cert  │ Either  │
    └────────┘ └────────┘ └────────┘

               ┌─────────────────────────────────┐
               │  PkiSecurityConfig              │
               │  @ConditionalOnProperty         │
               │  pki.enabled=true               │
               │                                 │
               │ ├─ SSLContext Creation          │
               │ ├─ KeyStore Loading             │
               │ ├─ TrustStore Loading           │
               │ ├─ Certificate Validator Bean   │
               │ └─ PKI Filter Registration      │
               └─────────────────────────────────┘

               ┌─────────────────────────────────┐
               │  PkiCertificateValidator        │
               │  @Component                     │
               │                                 │
               │ ├─ validateCertificate()        │
               │ ├─ extractCommonName()          │
               │ ├─ extractOrganization()        │
               │ ├─ getSerialNumber()            │
               │ └─ isCertificateValid()         │
               └─────────────────────────────────┘
```

---

## Data Flow Diagram

```
OAuth2 Request Flow:
┌─────────┐      ┌──────────────┐      ┌────────────┐      ┌─────────┐
│ Client  │─────>│ Spring Boot  │─────>│ Database   │─────>│Response │
│ (Auth)  │      │ (JwtFilter)  │      │ (User/Role)│      │ (Token) │
└─────────┘      └──────────────┘      └────────────┘      └─────────┘
     │                   │                    │                   │
     └─ username/pwd─────┘                    │                   │
                         └─ verify user ──────┘                   │
                                              └─ JWT + roles ─────┘

PKI Request Flow:
┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌─────────┐
│ Client  │─────>│ Spring Boot  │─────>│ PkiValidator │─────>│Response │
│(Cert)   │      │ (TLS + Filter)       │ (Cert Check) │      │ (Data)  │
└─────────┘      └──────────────┘      └──────────────┘      └─────────┘
     │                   │                    │                   │
     └─ certificate─────┘                    │                   │
                    ┌────────────────────────┘                   │
                    │ (extract CN, O, C, etc.)                   │
                    │                                             │
                    └─ ROLE_PKI_USER + cert info ───────────────┘

General API Request Flow:
┌─────────────────────────────────────────────────────────────────┐
│ Incoming Request (HTTP/HTTPS)                                   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                ┌────────────▼────────────┐
                │ Is it Public Endpoint?  │
                └────────┬──────┬─────────┘
                         │Yes   │No
                   Allow  │      └──────────┐
                         │                  │
                    Response            ┌───▼────────────────────┐
                                        │ Extract Auth Method    │
                                        │ (JWT or Certificate)   │
                                        └────┬──────────┬────────┘
                                             │JWT      │Cert
                                        ┌────▼──┐  ┌───▼────┐
                                        │JwtAuth│  │PkiAuth │
                                        └───┬───┘  └───┬────┘
                                            │         │
                                        ┌───▼─────────▼───┐
                                        │ Check Roles     │
                                        │ & Permissions   │
                                        └───┬─────────────┘
                                            │
                              ┌─────────────▼─────────────┐
                              │ Authorized?               │
                              └──┬──────────────────┬────┘
                                 │Yes               │No
                            ┌────▼──────┐    ┌─────▼─────┐
                            │ Process    │    │ Forbidden │
                            │ Request    │    │  (403)    │
                            └────┬───────┘    └───────────┘
                                 │
                            ┌────▼──────┐
                            │ Response   │
                            │ (200/Error)│
                            └────────────┘
```

---

## Port Usage

```
HTTP  (OAuth2 & Public):  localhost:8080
HTTPS (PKI/mTLS):         localhost:8443
Database (PostgreSQL):    localhost:5432

With Docker Compose:
- Spring App:  http://localhost:8080 & https://localhost:8443
- PostgreSQL:  localhost:5432
- PgAdmin:     http://localhost:5050
```

---

## Certificate Chain

```
Root CA (ca-cert.pem)
    │
    ├─► Signs ─────────────────────────────► Server Certificate (server-cert.pem)
    │                                         │
    │                                         └─► Used for HTTPS
    │
    └─► Signs ─────────────────────────────► Client Certificate (client-cert.pem)
                                              │
                                              └─► Used for mTLS Authentication

Truststore (truststore.p12) contains:
    └─► Root CA Certificate
        (allows verification of any certificate signed by this CA)

Keystore (keystore.p12) contains:
    ├─► Server Certificate
    └─► Server Private Key
        (used for HTTPS connections)

Client Keystore (client-keystore.p12) contains:
    ├─► Client Certificate
    └─► Client Private Key
        (used for mTLS authentication)
```

---

## Request Authentication Decision Tree

```
                    ┌─ Incoming Request
                    │
                    ├─ Extract Headers & Body
                    │
          ┌─────────▼──────────────────────────┐
          │ Is it a Public Endpoint?           │
          │ (/api/token, /swagger-ui, etc.)    │
          └─┬───────────────────────────────┬──┘
            │ YES                           │ NO
            │                               │
       ┌────▼──────┐                  ┌────▼────────────────────┐
       │ Allow All │                  │ Extract Auth Info       │
       └────┬──────┘                  │                        │
            │                         │ JWT Token in Header?    │
            │                         │ Client Certificate?     │
            │                         │ Basic Auth?             │
            │                         └────┬────────────────────┘
            │                              │
            │                    ┌─────────┼─────────┐
            │                    │         │         │
            │                    ▼         ▼         ▼
            │                ┌───────┐ ┌──────┐ ┌──────┐
            │                │ JWT   │ │Cert  │ │Basic │
            │                │ Auth  │ │ Auth │ │ Auth │
            │                └───┬───┘ └──┬───┘ └───┬──┘
            │                    │        │         │
            │            ┌───────▼───┐    │         │
            │            │ JwtFilter │    │         │
            │            │ Validate  │    │         │
            │            │ & Load    │    │         │
            │            │ Roles     │    │         │
            │            └───────┬───┘    │         │
            │                    │    ┌───▼────┐    │
            │                    │    │PkiAuth │    │
            │                    │    │Filter  │    │
            │                    │    │Extract │    │
            │                    │    │ Cert   │    │
            │                    │    └───┬────┘    │
            │                    │        │         │
            │    ┌───────────────┼────────┼─────────┤
            │    │               │        │         │
            │    │         ┌─────▼───────▼┐        │
            │    │         │ Authenticate │        │
            │    │         │ & Load Auth  │        │
            │    │         │ Authorities  │        │
            │    │         └─────┬────────┘        │
            │    │               │                 │
            │    │        ┌──────▼──────────────┐  │
            │    │        │ Check @PreAuthorize│  │
            │    │        │ & hasRole()        │  │
            │    │        └──────┬─────────────┘  │
            │    │               │                │
            │    │        ┌──────▼──────┐         │
            │    │        │ Roles Match?│         │
            │    │        └──┬───────┬──┘         │
            │    │           │ YES   │ NO         │
            │    │      ┌────▼─┐ ┌──▼─────┐      │
            │    │      │ 200  │ │ 403    │      │
            │    │      │ OK   │ │Forbid  │      │
            │    │      └────┬─┘ └──┬─────┘      │
            │    │           │      │            │
            └────┼───────────┼──────┼────────────┘
                 │           │      │
                 └───────────▼──────▼─────
                      Response
```

---

**Architecture Last Updated:** April 13, 2026  
**Diagrams Created For:** Dual Authentication System (OAuth2 + PKI)
