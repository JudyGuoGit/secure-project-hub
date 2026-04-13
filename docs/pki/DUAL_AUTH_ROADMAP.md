# Dual Authentication Implementation Roadmap

## Overview

This document outlines a step-by-step approach to add PKI/mTLS support to your existing OAuth2/JWT system.

## Current State vs Future State

### Current State (OAuth2/JWT Only)
```
┌─────────────────────────────────┐
│   Secure Project Hub            │
│                                 │
│  ┌──────────────────────────┐  │
│  │  All Requests            │  │
│  │  ├─ User Login → JWT     │  │
│  │  └─ All APIs → JWT Auth  │  │
│  └──────────────────────────┘  │
│                                 │
│  Suitable for: Web, Mobile, API │
│  Not suitable: Service-to-      │
│                service,          │
│                High-security     │
└─────────────────────────────────┘
```

### Future State (OAuth2/JWT + PKI/mTLS)
```
┌──────────────────────────────────────────┐
│     Secure Project Hub                   │
│                                          │
│  ┌──────────────────────────────────┐  │
│  │  User Requests                   │  │
│  │  ├─ Login → JWT Token            │  │
│  │  ├─ Web UI → JWT Auth            │  │
│  │  └─ Mobile → JWT Auth            │  │
│  └──────────────────────────────────┘  │
│                                          │
│  ┌──────────────────────────────────┐  │
│  │  Internal Service Requests       │  │
│  │  ├─ Service A → Cert (mTLS)      │  │
│  │  ├─ Service B → Cert (mTLS)      │  │
│  │  └─ Audit Service → Cert (mTLS)  │  │
│  └──────────────────────────────────┘  │
│                                          │
│  ┌──────────────────────────────────┐  │
│  │  Public API (Partner)            │  │
│  │  ├─ OAuth2 (standard)            │  │
│  │  └─ mTLS (enterprise partners)   │  │
│  └──────────────────────────────────┘  │
│                                          │
│  Suitable for: Everything!              │
│  Flexibility: Maximum                   │
└──────────────────────────────────────────┘
```

## Implementation Phases

### Phase 1: Research & Planning (Current)
- ✅ Understand PKI vs OAuth2
- ✅ Understand mutual TLS (mTLS)
- ✅ Plan architecture
- [ ] Set up development environment for certificates

**Duration**: 1-2 hours  
**Effort**: Low  
**Risk**: None

---

### Phase 2: Infrastructure Setup
#### 2a: Certificate Authority Setup
```
├── Create Root CA
│   ├── Generate Root CA private key
│   ├── Self-sign Root CA certificate
│   └── Store securely
├── Create Intermediate CA (optional but recommended)
│   ├── Generate Intermediate key
│   ├── Create CSR
│   └── Sign with Root CA
└── Store in secure location
    ├── Root CA public cert
    ├── Intermediate CA public cert (if used)
    └── Keep private keys secure
```

#### 2b: Server Certificate
```
├── Generate server private key
├── Create server certificate signed by CA
├── Convert to PKCS12 format (.p12)
├── Store in keystore
└── Configure Spring Boot to use
```

#### 2c: Client Certificate (for testing)
```
├── Generate client private key
├── Create client certificate signed by CA
├── Convert to PKCS12 format (.p12)
└── Use for testing service-to-service
```

**Duration**: 2-3 hours  
**Effort**: Medium  
**Files Created**:
- Root CA cert and key
- Server cert and key
- Test client cert and key
- Truststore (for verification)
- Keystore (for server)

---

### Phase 3: Spring Boot Configuration

#### 3a: Update application.yml
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEY_STORE_PASSWORD}
    key-store-type: PKCS12
    client-auth: want  # or 'need' for required mTLS
    enabled-protocols: TLSv1.2,TLSv1.3

# Trust store for verifying client certificates
trust:
  store:
    path: classpath:truststore.p12
    password: ${TRUST_STORE_PASSWORD}
```

#### 3b: Create Dual Authentication Filters
```
SecurityConfig
├── JWT Token Filter (existing)
│   └── Validates Authorization: Bearer <token>
├── Client Certificate Filter (new)
│   └── Extracts certificate from request
│   └── Validates certificate chain
│   └── Maps certificate to user/service
└── Authentication Manager
    └── Tries JWT first, then certificate
```

#### 3c: Create Certificate Validation Service
```
CertificateValidator
├── Verify certificate signature
├── Check expiration
├── Validate certificate chain
├── Check revocation (CRL/OCSP)
├── Extract subject information
└── Map to Spring Security Principal
```

**Duration**: 3-4 hours  
**Effort**: Medium-High  
**Dependencies**: 
- Spring Security
- Java Keystore tools
- Bouncy Castle (optional, for advanced crypto)

---

### Phase 4: Endpoint Configuration

#### 4a: Authentication Annotations
```java
// Current: JWT only
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> adminEndpoint() { }

// Enhanced: JWT or Certificate
@PreAuthorize("hasRole('ADMIN') or hasCertificate('service')")
public ResponseEntity<?> adminEndpoint() { }

// Service-to-service: Certificate only
@PreAuthorize("hasCertificate('internal-service')")
public ResponseEntity<?> internalEndpoint() { }
```

#### 4b: Create Custom @PreAuthorize Rules
```
@PreAuthorize Expressions:
├── hasJWTToken()         → JWT authentication
├── hasCertificate()      → Certificate authentication
├── hasValidCert()        → Valid, non-expired cert
├── certIssuer('...')     → Cert issued by specific CA
└── canAccess(resource)   → Dual auth support
```

**Duration**: 2-3 hours  
**Effort**: Low-Medium

---

### Phase 5: Testing & Validation

#### 5a: Unit Tests
```
CertificateValidatorTests
├── Valid certificate → Pass
├── Expired certificate → Fail
├── Wrong CA → Fail
├── Revoked certificate → Fail
└── Certificate chain validation

AuthenticationFilterTests
├── JWT token alone → Authenticate (JWT)
├── Certificate alone → Authenticate (mTLS)
├── Both present → Authenticate (prefer JWT)
└── Neither → Reject
```

#### 5b: Integration Tests
```
DualAuthenticationTests
├── User with JWT → Can access user endpoints
├── Service with certificate → Can access internal endpoints
├── User with cert → Behavior depends on rules
└── Invalid cert + JWT → JWT authentication wins
```

#### 5c: Manual Testing
```
Test Scenarios:
├── curl with JWT token
├── curl with client certificate
├── curl with both
├── curl with neither
├── Expired certificate
└── Certificate from untrusted CA
```

**Duration**: 3-4 hours  
**Effort**: Medium  
**Tools Needed**:
- curl with certificate support
- Postman/Insomnia with mTLS
- JUnit/Mockito

---

### Phase 6: Documentation

#### 6a: Operational Documentation
- Certificate generation procedures
- Certificate rotation process
- Revocation handling
- Troubleshooting guide
- Monitoring and alerts

#### 6b: Developer Documentation
- How to request certificates
- Testing with certificates
- Certificate formats and conversion
- Integration examples

#### 6c: Architecture Documentation
- Decision rationale
- Security implications
- Scalability considerations
- Performance impact

**Duration**: 2-3 hours  
**Effort**: Low-Medium  
**Output**:
- Operational runbook
- Developer guide
- Architecture decision record

---

## Timeline & Effort Summary

| Phase | Duration | Effort | Priority |
|-------|----------|--------|----------|
| Phase 1: Research | 1-2 hrs | Low | Critical |
| Phase 2: Infrastructure | 2-3 hrs | Medium | Critical |
| Phase 3: Configuration | 3-4 hrs | Medium-High | Critical |
| Phase 4: Endpoints | 2-3 hrs | Low-Medium | High |
| Phase 5: Testing | 3-4 hrs | Medium | High |
| Phase 6: Documentation | 2-3 hrs | Low-Medium | Medium |
| **Total** | **13-19 hrs** | **Medium** | - |

---

## Recommended Implementation Sequence

### For MVP (Minimum Viable Product)
1. **Phase 1**: Understand the concepts ✅ (Done)
2. **Phase 2a**: Create Root CA
3. **Phase 2b**: Create Server certificate
4. **Phase 3**: Basic Spring Boot configuration
5. **Phase 4**: Add `/api/internal/health` endpoint with mTLS
6. **Phase 5**: Test with curl

**Time**: 4-6 hours  
**Result**: Proof of concept - OAuth2 + mTLS working together

### For Production Ready
- Complete all phases
- Add certificate rotation automation
- Implement certificate revocation (CRL/OCSP)
- Add monitoring and alerting
- Load testing with mTLS
- Security audit

**Time**: 25-40 hours  
**Result**: Enterprise-grade dual authentication

---

## Architecture Diagram: Complete Flow

```
┌────────────────────────────────────────────────────────────────┐
│                    Client Requests                             │
└────────────────────────────────────────────────────────────────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
                ▼              ▼              ▼
          ┌─────────┐    ┌─────────┐    ┌──────────┐
          │ Web     │    │ Mobile  │    │ Service  │
          │ Browser │    │ App     │    │ A        │
          └─────────┘    └─────────┘    └──────────┘
                │              │              │
                │              │              │
       OAuth2/JWT      OAuth2/JWT       mTLS/PKI
                │              │              │
                └──────────────┼──────────────┘
                               │
                      ┌────────▼────────┐
                      │  TLS/HTTP Layer │
                      └────────┬────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
              ▼                ▼                ▼
    ┌─────────────────────────────────────────────┐
    │    Spring Security Filter Chain             │
    │                                             │
    │ ┌─────────────────────────────────────────┐ │
    │ │ Is mTLS Request?                        │ │
    │ │ (Client certificate present)            │ │
    │ │                                         │ │
    │ │ YES → Certificate Filter                │ │
    │ │ ├─ Extract certificate                  │ │
    │ │ ├─ Validate chain                       │ │
    │ │ ├─ Check expiration                     │ │
    │ │ ├─ Verify issuer                        │ │
    │ │ └─ Create Principal                     │ │
    │ │                                         │ │
    │ │ NO → Check JWT                          │ │
    │ │ ├─ Extract token from header            │ │
    │ │ ├─ Validate signature                   │ │
    │ │ ├─ Check expiration                     │ │
    │ │ └─ Create Principal                     │ │
    │ │                                         │ │
    │ │ NEITHER → Reject (401)                  │ │
    │ └─────────────────────────────────────────┘ │
    └─────────────┬───────────────────────────────┘
                  │
        ┌─────────▼─────────┐
        │ Principal Created │
        │ Roles/Authorities │
        │ Set               │
        └─────────┬─────────┘
                  │
        ┌─────────▼──────────────┐
        │ @PreAuthorize Check    │
        │ ├─ Allowed?           │
        │ │ YES → 200 OK        │
        │ │ NO  → 403 Forbidden │
        │ └─ Execute endpoint   │
        └────────────────────────┘
```

---

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| Certificate expiration | Medium | High | Automated rotation, monitoring |
| Private key compromise | Low | Critical | HSM, secure storage, immediate revocation |
| Certificate validation bugs | Medium | High | Thorough testing, security audit |
| Performance degradation | Low | Medium | Load testing, optimization |
| Compatibility issues | Low | Medium | Gradual rollout, backward compatibility |

---

## Success Criteria

- ✅ OAuth2/JWT continues to work 100%
- ✅ mTLS/PKI endpoints work for service-to-service
- ✅ Both authentication methods work simultaneously
- ✅ Zero security regression
- ✅ Performance impact < 5%
- ✅ All tests pass
- ✅ Documentation complete

---

## Next Steps

Ready to proceed with implementation? I recommend starting with **Phase 2a** (Certificate Authority Setup).

Would you like me to:
1. Create scripts to generate certificates?
2. Implement Phase 3 (Spring Boot configuration)?
3. Create test certificates for demonstration?
4. All of the above?

Let me know! 🚀
