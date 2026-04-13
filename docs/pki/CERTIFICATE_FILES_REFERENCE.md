# Certificate Files - Naming Convention Reference

## Naming Convention

### Files ending with `-cert.pem` = PUBLIC (Safe to share)
### Files ending with `-key.pem` = PRIVATE (Keep secret)

---

## Detailed Breakdown

### Root Certificate Authority (CA)

| File | Type | Public/Private | Readable | Git | Purpose |
|------|------|----------------|----------|-----|---------|
| `ca-cert.pem` | Public Certificate | 🟢 **PUBLIC** | Yes (644) | ✅ YES | Root CA public certificate - used to sign all other certs |
| `ca-key.pem` | Private Key | 🔴 **PRIVATE** | Owner only (600) | ❌ NO | Root CA private key - **KEEP IN VAULT** |

---

### Server Certificates (for HTTPS)

| File | Type | Public/Private | Readable | Git | Purpose |
|------|------|----------------|----------|-----|---------|
| `server-cert.pem` | Public Certificate | 🟢 **PUBLIC** | Yes (644) | ✅ YES | Server public certificate - sent to clients |
| `server-key.pem` | Private Key | 🔴 **PRIVATE** | Owner only (600) | ❌ NO | Server private key - decrypt client messages |

---

### Client Certificates (for mTLS Testing)

| File | Type | Public/Private | Readable | Git | Purpose |
|------|------|----------------|----------|-----|---------|
| `client-cert.pem` | Public Certificate | 🟢 **PUBLIC** | Yes (644) | ✅ YES | Client public certificate - for testing |
| `client-key.pem` | Private Key | 🔴 **PRIVATE** | Owner only (600) | ❌ NO | Client private key - for testing only |

---

### Keystores (Binary Formats)

| File | Contents | Type | Permissions | Git | Location | Purpose |
|------|----------|------|-------------|-----|----------|---------|
| `keystore.p12` | server-cert.pem + server-key.pem | 🔴 **PRIVATE** | 600 | ❌ NO | `src/main/resources/` | Server keystore for HTTPS |
| `truststore.p12` | ca-cert.pem | 🟢 **PUBLIC** | 644 | ✅ YES | `src/main/resources/` | Validate client certs |
| `client-keystore.p12` | client-cert.pem + client-key.pem | 🔴 **PRIVATE** | 600 | ❌ NO | `certs/` (testing) | Test mTLS endpoints |

---

## Memory Aid

```
-cert = Certificate (Public) = 📤 Can distribute
-key  = Private Key (Secret) = 🔒 Keep secure

Examples:
✅ ca-cert.pem         → Public
❌ ca-key.pem          → Private
✅ server-cert.pem     → Public
❌ server-key.pem      → Private
✅ client-cert.pem     → Public
❌ client-key.pem      → Private
```

---

## Security Rules

### 🟢 Files Safe to Commit (Public Certificates)
```
ca-cert.pem
server-cert.pem
client-cert.pem
truststore.p12 (contains only ca-cert.pem)
```

### 🔴 Files NEVER Commit (Private Keys)
```
ca-key.pem                    ← CRITICAL!
server-key.pem                ← CRITICAL!
client-key.pem
keystore.p12                  ← Contains private keys
client-keystore.p12           ← Contains private keys
```

---

## File Permissions Explained

```
-rw-r--r--  (644) = Everyone can read    = 🟢 PUBLIC
-rw-------  (600) = Owner only           = 🔴 PRIVATE
```

When you see:
- `-rw-r--r--` with `-cert.pem` → Public file ✅
- `-rw-------` with `-key.pem`  → Private file 🔒

---

## How They Work Together

```
PUBLIC CERTIFICATE (-cert.pem)
├─ Can be read by anyone
├─ Distributed to clients
└─ Used to verify signatures

    + 

PRIVATE KEY (-key.pem)
├─ Never shared
├─ Kept secure
└─ Used to sign/decrypt messages

    =

CERTIFICATE PAIR
├─ Public half: shared with the world
└─ Private half: kept in vault
```

---

## Real-World Analogy

Think of it like a **lock and key**:

- **Public Certificate (-cert.pem)** = The lock
  - Anyone can see it
  - Used to lock a message
  - Safe to publish

- **Private Key (-key.pem)** = The key
  - Only you have it
  - Used to unlock a message
  - Must be kept secret

Together they allow secure communication where:
- Client can verify the server's identity using `server-cert.pem`
- Server can verify the client's identity using `client-cert.pem`
- Only the holder of `-key.pem` can prove they own the certificate

---

## Summary

| Suffix | Type | Security | Share? | Commit? | Permission |
|--------|------|----------|--------|---------|-----------|
| `-cert.pem` | Public Certificate | 🟢 Safe | ✅ Yes | ✅ Yes | 644 |
| `-key.pem` | Private Key | 🔴 Secret | ❌ No | ❌ No | 600 |

**Your question:** "The file with '_cert' is public and '_key' is private, correct?"

**Answer:** ✅ **100% CORRECT!**
