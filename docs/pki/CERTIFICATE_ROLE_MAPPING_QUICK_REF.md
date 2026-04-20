# Quick Reference: Certificate Role Mapping

## TL;DR

**Problem:** All PKI clients got ADMIN role (not secure)  
**Solution:** Smart role mapping from certificate OU + optional DB overrides

---

## How It Works

1. **Client sends certificate with OU=ADMIN**
   - ✅ Automatically gets `ROLE_ADMIN` (no DB lookup needed)

2. **Client sends certificate with OU=READONLY**
   - ✅ Automatically gets `ROLE_PKI_USER` (safest default)

3. **Need to override? Add DB record:**
   ```sql
   INSERT INTO certificate_role_mapping (certificate_cn, override_role_id, created_by, notes)
   VALUES ('special-client', 1, 'admin@example.com', 'Special access');
   ```
   - ✅ Same client now gets `ROLE_ADMIN` from DB override

---

## OU to Role Mapping

| OU | Role | When to Use |
|----|------|-----------|
| `ADMIN` | `ROLE_ADMIN` | Admin clients |
| `OPERATIONS` | `ROLE_USER` | Ops team |
| `READONLY` | `ROLE_VIEWER` | Read-only clients |
| (anything else) | `ROLE_PKI_USER` | Default/unknown |

---

## API Commands

### Add Override
```bash
curl -X POST \
  'http://localhost:8080/api/admin/certificate-roles?certificateCN=my-client&roleName=ADMIN' \
  -H 'Authorization: Bearer <JWT>'
```

### Remove Override
```bash
curl -X DELETE \
  'http://localhost:8080/api/admin/certificate-roles?certificateCN=my-client' \
  -H 'Authorization: Bearer <JWT>'
```

---

## Generate Certs with OU

```bash
# Admin certificate
openssl req -new -key key.pem \
  -subj "/CN=admin-client/OU=ADMIN/O=Secure-Project-Hub/C=US"

# Readonly certificate
openssl req -new -key key.pem \
  -subj "/CN=readonly-client/OU=READONLY/O=Secure-Project-Hub/C=US"
```

---

## How Authorization Works

1. **Certificate arrives** → Extract OU
2. **Check DB** for override (optional)
3. **Resolve role** → `ROLE_PKI_USER` + resolved role
4. **@PreAuthorize check** uses resolved roles
5. **Access granted/denied** based on roles

---

## Files

| File | Purpose |
|------|---------|
| `CertificateRoleService.java` | Role resolution logic |
| `CertificateRoleMapping.java` | Entity for DB records |
| `CertificateRoleController.java` | REST API |
| `certificate_role_mapping` table | DB storage |

---

## Testing

**Test PKI auth:**
```bash
curl -s --cert client-cert.pem --key client-key.pem \
  --cacert ca-cert.pem https://localhost:8443/api/users
```

**Verify role in logs:**
```bash
docker logs secure-project-hub-app-1 | grep "Certificate CN"
```

---

## Troubleshooting

**Q: Client gets wrong role**  
A: Check certificate OU or DB override

**Q: Want to grant ADMIN to specific cert?**  
A: Add DB record via POST endpoint

**Q: DB override not working?**  
A: Make sure certificate CN matches exactly

---

**See Also:** `docs/pki/CERTIFICATE_ROLE_MAPPING.md` for full docs
