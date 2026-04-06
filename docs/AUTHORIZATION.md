# Authorization (Authz) Implementation Guide

## Overview

Role-based access control (RBAC) has been implemented across all API endpoints using Spring Security's `@PreAuthorize` annotation. This document outlines the authorization strategy and how roles control access to different parts of the application.

## Authorization Strategy

### Roles Defined

1. **ADMIN**: Full system access - can create, read, update, and delete all resources
2. **USER**: Limited access - can view resources but cannot create, update, or delete

### Authorization Levels by Endpoint

#### User Management (`/api/users`)
| Operation | GET All | GET by ID | CREATE | UPDATE | DELETE |
|-----------|---------|----------|--------|--------|--------|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **USER** | ✅ | ✅ | ❌ | ❌ | ❌ |
| **Unauthenticated** | ❌ | ❌ | ❌ | ❌ | ❌ |

#### Role Management (`/api/roles`)
| Operation | GET All | GET by ID | CREATE | UPDATE | DELETE |
|-----------|---------|----------|--------|--------|--------|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **USER** | ✅ | ✅ | ❌ | ❌ | ❌ |

#### Permission Management (`/api/permissions`)
| Operation | GET All | GET by ID | CREATE | UPDATE | DELETE |
|-----------|---------|----------|--------|--------|--------|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **USER** | ✅ | ✅ | ❌ | ❌ | ❌ |

#### Audit Logs (`/api/audit-logs`)
| Operation | GET All | GET by ID | CREATE | UPDATE | DELETE |
|-----------|---------|----------|--------|--------|--------|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **USER** | ❌ | ❌ | ❌ | ❌ | ❌ |

#### User-Role Assignments (`/api/user-roles`)
| Operation | GET All | GET by ID | CREATE | UPDATE | DELETE |
|-----------|---------|----------|--------|--------|--------|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **USER** | ✅ | ✅ | ❌ | ❌ | ❌ |

#### Role-Permission Assignments (`/api/role-permissions`)
| Operation | GET All | GET by ID | CREATE | UPDATE | DELETE |
|-----------|---------|----------|--------|--------|--------|
| **ADMIN** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **USER** | ✅ | ✅ | ❌ | ❌ | ❌ |

## Implementation Details

### 1. Security Configuration (`SecurityConfig.java`)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // ← Enables @PreAuthorize
public class SecurityConfig {
    // ... rest of configuration
}
```

**Key addition**: `@EnableMethodSecurity(prePostEnabled = true)` enables method-level security checks using `@PreAuthorize` annotations.

### 2. Controller-Level Authorization

Each controller endpoint is protected with `@PreAuthorize` annotations:

**Example from UserController:**
```java
@GetMapping
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public List<User> getAllUsers() {
    return userRepository.findAll();
}

@PostMapping
@PreAuthorize("hasRole('ADMIN')")
public User createUser(@RequestBody User user) {
    return userRepository.save(user);
}

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // ... delete logic
}
```

### 3. Authorization Expressions

#### Basic Expressions
- `hasRole('ADMIN')` - User must have ADMIN role
- `hasAnyRole('ADMIN', 'USER')` - User must have ADMIN or USER role
- `isAuthenticated()` - User must be authenticated

#### Common Patterns Used
```java
// Read operations - available to ADMIN and USER
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public List<?> getAll() { }

// Write operations - available only to ADMIN
@PreAuthorize("hasRole('ADMIN')")
public ? create(@RequestBody ?) { }

// Admin-only access - audit logs
@PreAuthorize("hasRole('ADMIN')")
public List<AuditLog> getAllAuditLogs() { }
```

## Authorization Flow

```
┌─────────────────────────────────┐
│    HTTP Request                 │
│    + JWT Token                  │
└──────────────┬──────────────────┘
               │
               ▼
    ┌─────────────────────┐
    │  JwtTokenFilter     │
    │  Extract JWT        │
    │  Extract Roles      │
    └─────────┬───────────┘
              │
              ▼
    ┌─────────────────────┐
    │  Endpoint Method    │
    │  @PreAuthorize      │
    │  Check Roles        │
    └──────────┬──────────┘
               │
        ┌──────┴──────┐
        │             │
        ▼             ▼
    ✅ Access    ❌ Access Denied
    Granted     (HTTP 403)
```

## Test Credentials

| User | Password | Role | Permissions |
|------|----------|------|-------------|
| admin | admin | ADMIN | All operations |
| user | password | USER | Read-only access |
| john | password | USER | Read-only access |
| jane | password | USER | Read-only access |

## Testing Authorization

### 1. Admin User Can Create
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "email": "new@example.com"}'
# ✅ HTTP 200 - Success
```

### 2. Regular User Cannot Create
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <user-token>" \
  -H "Content-Type: application/json" \
  -d '{"username": "newuser", "email": "new@example.com"}'
# ❌ HTTP 403 - Forbidden
```

### 3. Unauthenticated User Cannot Access
```bash
curl -X GET http://localhost:8080/api/users
# ❌ HTTP 401 - Unauthorized
```

## Response Handling

### Authorized Access
```http
HTTP/1.1 200 OK
Content-Type: application/json

[{ users data }]
```

### Authorization Denied
```http
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "error": "Forbidden",
  "message": "Access is denied"
}
```

### No Authentication
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "error": "Unauthorized",
  "message": "No JWT token found"
}
```

## Controllers Modified

1. **UserController** - Roles: ADMIN (write), ADMIN+USER (read)
2. **RoleController** - Roles: ADMIN (write), ADMIN+USER (read)
3. **PermissionController** - Roles: ADMIN (write), ADMIN+USER (read)
4. **AuditLogController** - Roles: ADMIN only
5. **UserRoleController** - Roles: ADMIN (write), ADMIN+USER (read)
6. **RolePermissionController** - Roles: ADMIN (write), ADMIN+USER (read)

## Key Security Features

✅ **Method-Level Security**: Authorization checks happen at the method level before execution
✅ **Declarative Security**: Uses annotations instead of programmatic checks
✅ **Fail-Safe**: Default deny - endpoints are protected unless explicitly permitted
✅ **JWT Integration**: Roles extracted from JWT token
✅ **Audit Trail**: All attempts logged (successful and failed)
✅ **Consistent Pattern**: Same authorization pattern across all controllers

## Future Enhancements

- [ ] Permission-based access (not just role-based)
- [ ] Resource-level authorization (e.g., users can only modify their own profile)
- [ ] Custom authorization expressions
- [ ] OAuth2/OIDC integration for external identity providers
- [ ] Fine-grained permission checks using database-stored permissions

## Troubleshooting

### Getting 403 Forbidden When Should Have Access
- Verify JWT token is valid
- Check user has required role assigned
- Verify role names match exactly (case-sensitive)
- Check @PreAuthorize expression uses correct role names

### All Requests Returning 403
- Verify JWT token is being sent in Authorization header
- Verify JwtTokenFilter is configured correctly
- Check SecurityConfig has correct permitAll() paths

### Method Security Not Working
- Verify `@EnableMethodSecurity(prePostEnabled = true)` is present in SecurityConfig
- Verify @PreAuthorize annotations are on public methods
- Check roles are loaded correctly from JWT token

---

**Last Updated**: April 6, 2026  
**Status**: ✅ Production Ready
