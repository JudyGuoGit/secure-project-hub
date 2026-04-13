# Documentation Structure

This directory contains all documentation for the Secure Project Hub application.

## Directory Organization

```
docs/
├── README.md                           (this file)
├── ARCHITECTURE_DIAGRAMS.md            System architecture and design
├── AUTHORIZATION.md                    Authorization concepts and framework
├── IMPLEMENTATION_SUMMARY.md           Overall implementation summary
├── MEMORY_CONFIGURATION.md             Memory tuning and optimization guide
├── YOUR_UNDERSTANDING_CONFIRMED.md     Understanding confirmation document
│
├── pki/                                PKI/mTLS Authentication Documentation
│   ├── PKI_QUICK_START.md             Quick start guide for PKI setup
│   ├── PKI_TESTING_GUIDE.md           Local PKI testing instructions
│   ├── PKI_TESTING_GUIDE_DOCKER.md    Docker PKI testing instructions
│   ├── PKI_vs_OAUTH2.md               Comparison: PKI vs OAuth2
│   ├── PKI_vs_SSH_KEYS.md             Comparison: PKI vs SSH Keys
│   ├── CA_QUICK_REFERENCE.md          CA certificate quick reference
│   ├── CA_CERTIFICATE_EXPLAINED.md    CA certificate detailed explanation
│   ├── CERTIFICATE_DISTRIBUTION_GUIDE.md  How to distribute certificates
│   ├── CERTIFICATE_FILES_REFERENCE.md    Certificate files reference
│   ├── DUAL_AUTH_QUICK_START.md       Dual authentication quick start
│   ├── DUAL_AUTH_ROADMAP.md           Dual authentication roadmap
│   ├── DUAL_AUTHENTICATION_SETUP.md   Dual authentication setup
│   └── DUAL_AUTHENTICATION_IMPLEMENTATION.md  Implementation details
│
└── tests/                              Testing Scripts and Tools
    ├── pki-test-suite.sh              Complete PKI test suite
    ├── pki-test-quick-ref.sh          Quick reference for PKI tests
    └── test_pki.py                    Python PKI endpoint tester
```

## Quick Navigation

### For Getting Started
- Start with: **ARCHITECTURE_DIAGRAMS.md** → understand the system design
- Then read: **pki/PKI_QUICK_START.md** → learn PKI basics
- Setup: **pki/DUAL_AUTH_QUICK_START.md** → configure dual authentication

### For Testing
- Run: `docs/tests/pki-test-suite.sh` → full test suite
- Reference: `docs/tests/pki-test-quick-ref.sh` → quick test reference
- Python: `docs/tests/test_pki.py` → run Python test script

### For Configuration
- Memory optimization: **MEMORY_CONFIGURATION.md**
- Authorization setup: **AUTHORIZATION.md**
- Implementation details: **IMPLEMENTATION_SUMMARY.md**

### For Troubleshooting
- Certificate issues: **pki/CA_CERTIFICATE_EXPLAINED.md**
- Distribution problems: **pki/CERTIFICATE_DISTRIBUTION_GUIDE.md**
- Testing locally: **pki/PKI_TESTING_GUIDE.md**
- Testing in Docker: **pki/PKI_TESTING_GUIDE_DOCKER.md**

## Key Features Documented

### PKI/mTLS Authentication
- Certificate-based mutual authentication (mTLS)
- Client certificate validation
- Certificate distribution and management
- Integration with REST API

### Dual Authentication
- Combined PKI and OAuth2/JWT support
- Transparent authentication switching
- Role-based access control (RBAC)

### Memory Optimization
- Docker memory limits configuration
- JVM heap tuning
- Performance optimization guidelines

## File Sizes at a Glance

| File | Purpose |
|------|---------|
| pki-test-suite.sh | Full automated PKI test runner |
| test_pki.py | Python test script for endpoints |
| pki-test-quick-ref.sh | Quick reference and utility functions |

## Updates and Maintenance

Last updated: April 13, 2026
- Added memory configuration documentation
- Reorganized PKI-related docs into `pki/` subfolder
- Organized test scripts into `tests/` subfolder
- Integrated PKI with REST API (dual authentication)
