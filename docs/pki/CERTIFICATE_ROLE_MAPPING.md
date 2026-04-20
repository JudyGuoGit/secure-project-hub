# Certificate-Based Role Mapping System

## Overview

The **Certificate Role Mapping System** provides intelligent, flexible authentication for PKI/mTLS clients:

1. **Default Role from Certificate OU** - Automatic role assignment based on certificate attributes
2. **Database Override** - Optional privilege escalation for specific certificates
3. **Zero Configuration for Most** - Only add DB records when needed for special cases

---

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Client Request with X.509 Certificate                       │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  PkiAuthenticationFilter Extracts Certificate Attributes     │
│  - CN (Common Name)      e.g., "test-client"                 │
│  - OU (Organizational Unit) e.g., "ADMIN"                    │
│  - O (Organization)      e.g., "Secure-Project-Hub"          │
│  - Serial #              e.g., "196813120748488..."           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  CertificateRoleService Resolves Role                        │
│  1. Check DB for certificate CN override?                    │
│  2. If YES → Use override role (ADMIN, USER, etc.)           │
│  3. If NO → Map OU to default role                           │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  Spring Security Context Set with Resolved Roles             │
│  - ROLE_PKI_USER (always added)                              │
│  - ROLE_ADMIN / ROLE_USER / ROLE_VIEWER (based on mapping)   │
│  - Organization-based role (ROLE_SECURE_PROJECT_HUB)         │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│  @PreAuthorize Authorization Checked                         │
│  e.g., @PreAuthorize("hasRole('ADMIN')")                     │
└─────────────────────────────────────────────────────────────┘
```

---

## Default Role Mapping from Certificate OU

If **NO database override** exists, the OU (Organizational Unit) determines the default role:

| Certificate OU | Mapped Role | Use Case |
|---|---|---|
| `ADMIN`, `ADMINISTRATORS`, `ADMIN_TEAM` | `ROLE_ADMIN` | System administrators |
| `OPERATIONS`, `OPS`, `OPS_TEAM` | `ROLE_USER` | Operations team members |
| `READONLY`, `READ_ONLY`, `VIEWER` | `ROLE_VIEWER` | Read-only access |
| (Any other) | `ROLE_PKI_USER` | Default (lowest privilege) |

---

## Database Override System

For special cases where you need to escalate a certificate to ADMIN (or any role):

### Table: `certificate_role_mapping`

```sql
CREATE TABLE certificate_role_mapping (
    id SERIAL PRIMARY KEY,
    certificate_cn VARCHAR(255) NOT NULL UNIQUE,    -- Client cert CN
    certificate_serial VARCHAR(255),                 -- Optional: serial for additional validation
    override_role_id BIGINT NOT NULL,               -- Role to assign
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),                        -- Who made the change
    notes TEXT,                                     -- Why this mapping exists
    CONSTRAINT fk_override_role FOREIGN KEY (override_role_id) REFERENCES roles(id)
);
```

### Examples

**Example 1: Grant admin to a specific client**
```sql
-- Find the ADMIN role ID (usually 1)
SELECT id FROM roles WHERE name = 'ADMIN';

-- Add certificate mapping
INSERT INTO certificate_role_mapping (certificate_cn, override_role_id, created_by, notes)
VALUES ('admin-client', 1, 'admin@example.com', 'Special admin certificate for test environment');
```

**Example 2: Grant USER role to a certificate that has OU=READONLY**
```sql
INSERT INTO certificate_role_mapping (certificate_cn, override_role_id, created_by, notes)
VALUES ('readonly-escalated-client', 2, 'admin@example.com', 'Readonly client granted temporary USER access');
```

---

## API Endpoints

### Add Certificate Role Mapping
```bash
POST /api/admin/certificate-roles?certificateCN=test-client&roleName=ADMIN
Authorization: Bearer <JWT_TOKEN>
```

**Response:**
```json
{
  "id": 1,
  "certificateCN": "test-client",
  "overrideRole": {
    "id": 1,
    "name": "ADMIN"
  },
  "createdAt": "2026-04-14T10:50:00",
  "createdBy": null
}
```

### Remove Certificate Role Mapping
```bash
DELETE /api/admin/certificate-roles?certificateCN=test-client
Authorization: Bearer <JWT_TOKEN>
```

---

## Configuration

The system works automatically. No configuration required!

- **Default behavior**: Certs get roles based on their OU
- **Optional behavior**: Add DB records for escalation only when needed

### Example Certificate Generation

When generating test certificates, set the OU to control default role:

```bash
# Create an admin certificate (will get ROLE_ADMIN by default)
openssl req -new -key admin-client-key.pem \
  -out admin-client.csr \
  -subj "/CN=admin-client/O=Secure-Project-Hub/OU=ADMIN/C=US"

# Create a readonly certificate (will get ROLE_PKI_USER by default)
openssl req -new -key readonly-client-key.pem \
  -out readonly-client.csr \
  -subj "/CN=readonly-client/O=Secure-Project-Hub/OU=READONLY/C=US"
```

---

## How It Works in Code

### 1. Certificate Extraction (PkiAuthenticationFilter)
```java
String cn = certificateValidator.extractCommonName(clientCert);
String ou = certificateValidator.extractOrganizationalUnit(clientCert);

// Resolve role: DB override → OU-based default → PKI_USER fallback
String resolvedRole = certificateRoleService.resolveCertificateRole(clientCert);

// Grant both base role and resolved role
authorities.add(new SimpleGrantedAuthority("ROLE_PKI_USER"));
authorities.add(new SimpleGrantedAuthority(resolvedRole));
```

### 2. Role Resolution (CertificateRoleService)
```java
public String resolveCertificateRole(X509Certificate clientCert) {
    String cn = certificateValidator.extractCommonName(clientCert);
    
    // Step 1: Check DB for override
    Optional<CertificateRoleMapping> mapping = 
        certificateRoleMappingRepository.findByCertificateCN(cn);
    
    if (mapping.isPresent()) {
        return "ROLE_" + mapping.get().getOverrideRole().getName().toUpperCase();
    }
    
    // Step 2: Map OU to default role
    String ou = certificateValidator.extractOrganizationalUnit(clientCert);
    return mapOUToRole(ou);
}
```

---

## Best Practices

### ✅ DO

- **Set OU when creating certificates** - This defines default roles
- **Document why DB overrides exist** - Use the `notes` field in the mapping table
- **Use overrides sparingly** - Most certs should get appropriate roles via OU
- **Validate certificate serial** - Store it for additional security

### ❌ DON'T

- Don't grant ALL certs ADMIN role (violates Zero Trust)
- Don't forget to set OU when creating certificates
- Don't create unmaintainable OU mappings
- Don't rely solely on CN (OU is more flexible)

---

## Security Considerations

1. **Lazy Evaluation** - Database is only queried if certificate CN needs role mapping
2. **Defaults are Safe** - Unknown OUs get `ROLE_PKI_USER` (lowest privilege)
3. **Always Require Authentication** - Even PKI_USER must be authenticated
4. **Admin Endpoint Protected** - Certificate mappings can only be modified by ADMIN
5. **Audit Trail** - `created_by` and `created_at` track all changes

---

## Testing

### Test with Default OU-Based Role
```bash
# Certificate with OU=ADMIN will get ROLE_ADMIN automatically
curl -s --cacert certs/ca-cert.pem \
     --cert certs/admin-client-cert.pem \
     --key certs/admin-client-key.pem \
     https://localhost:8443/api/users
```

### Test with Database Override
```bash
# Add override via API
curl -X POST \
  'http://localhost:8080/api/admin/certificate-roles?certificateCN=test-client&roleName=ADMIN' \
  -H 'Authorization: Bearer <JWT_TOKEN>'

# Test with cert (now has ADMIN even if OU doesn't say so)
curl -s --cacert certs/ca-cert.pem \
     --cert certs/test-client-cert.pem \
     --key certs/test-client-key.pem \
     https://localhost:8443/api/users
```

---

## Migration Scripts

### V8__Create_certificate_role_mapping_table.sql
Creates the `certificate_role_mapping` table for database overrides.

### V9__Seed_certificate_role_mappings.sql
(Optional) Seed initial mappings if needed. By default, it contains no data.

---

## FAQ

**Q: Do I need to add DB records for every certificate?**  
A: No! Only add records when you need to override the default OU-based role.

**Q: What if I forget to set the OU in the certificate?**  
A: It defaults to `ROLE_PKI_USER` (safest option).

**Q: Can I have multiple roles per certificate?**  
A: Yes! The system assigns both `ROLE_PKI_USER` and the resolved role.

**Q: How do I know which OU maps to which role?**  
A: See the "Default Role Mapping" table above. You can also customize it in `CertificateRoleService.mapOUToRole()`.

**Q: Can I use this with OAuth2 JWT tokens?**  
A: Yes! The system is independent - JWT roles work the same way via `JwtTokenFilter`.

---

## Related Files

| File | Purpose |
|------|---------|
| `CertificateRoleMapping.java` | Entity for database mappings |
| `CertificateRoleMappingRepository.java` | Data access layer |
| `CertificateRoleService.java` | Business logic for role resolution |
| `PkiAuthenticationFilter.java` | Authentication filter (updated) |
| `PkiCertificateValidator.java` | Certificate parsing (added extractOU) |
| `CertificateRoleController.java` | REST API for managing mappings |

---

**Last Updated:** 2026-04-14  
**System Status:** ✅ Ready for Production  
**Default Behavior:** Safe (least privilege)
