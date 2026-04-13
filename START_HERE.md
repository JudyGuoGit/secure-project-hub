# 🚀 START HERE

Welcome to **Secure Project Hub** - A secure API with dual authentication (PKI/mTLS + OAuth2/JWT).

## Quick Navigation

### 🔐 PKI/mTLS Testing
- **Quick Start:** See `docs/pki/PKI_QUICK_START.md`
- **Run Tests:** `bash docs/tests/pki-test-suite.sh`
- **Docker Testing:** `docs/pki/PKI_TESTING_GUIDE_DOCKER.md`

### 📚 Complete Documentation
**All documentation is organized in the `docs/` folder:**

```
docs/
├── README.md                    ← Navigation guide for all docs
├── pki/                        ← PKI/mTLS documentation
│   ├── PKI_QUICK_START.md
│   ├── PKI_TESTING_GUIDE_DOCKER.md
│   ├── DUAL_AUTH_QUICK_START.md
│   └── ... (13 files)
├── tests/                      ← Test scripts
│   ├── pki-test-suite.sh
│   ├── test_pki.py
│   └── pki-test-quick-ref.sh
└── ... (other docs)
```

### ⚡ Quick Start (5 minutes)

**1. Start the application:**
```bash
docker-compose up -d
```

**2. Run PKI tests:**
```bash
bash docs/tests/pki-test-suite.sh
```

**3. Test with PKI authentication:**
```bash
curl -s --cacert certs/ca-cert.pem \
     --cert certs/client-cert.pem \
     --key certs/client-key.pem \
     https://localhost:8443/api/users | jq .
```

## 🎯 Key Features

✅ **HTTPS-Only** - Port 8443 with mTLS support  
✅ **PKI/mTLS** - Client certificate authentication  
✅ **OAuth2/JWT** - Username/password token-based auth  
✅ **Dual Auth** - Both methods on same endpoints  
✅ **REST API** - `/api/users`, `/api/roles`, `/api/permissions`, etc.  
✅ **RBAC** - Role-based access control  

## 📖 Documentation Structure

| Folder | Purpose |
|--------|---------|
| `docs/` | Main documentation hub |
| `docs/pki/` | All PKI/mTLS related docs |
| `docs/tests/` | Test scripts and automation |

## 🔧 Configuration

- **App Port:** 8443 (HTTPS)
- **Database:** PostgreSQL on port 5432
- **Certificate Location:** `certs/`
- **Memory:** JVM 1024MB, Docker 2GB hard limit

## ✅ What's Working

- ✅ Out-of-Memory issue resolved
- ✅ PKI authentication fully integrated
- ✅ REST API accessible with PKI certificates
- ✅ Dual authentication (PKI + OAuth2)
- ✅ All tests passing

## 📝 Next Steps

1. **Explore the docs:** `docs/README.md`
2. **Run the tests:** `bash docs/tests/pki-test-suite.sh`
3. **Read the guides:** Start with `docs/pki/PKI_QUICK_START.md`
4. **Test the API:** Use PKI certificates to access REST endpoints

---

**For detailed documentation:** See `docs/README.md`
