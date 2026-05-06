package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.entity.AuditLog;
import com.judy.secureprojecthub.entity.Permission;
import com.judy.secureprojecthub.entity.Role;
import com.judy.secureprojecthub.entity.RolePermission;
import com.judy.secureprojecthub.entity.User;
import com.judy.secureprojecthub.entity.UserRole;
import com.judy.secureprojecthub.repository.AuditLogRepository;
import com.judy.secureprojecthub.repository.RolePermissionRepository;
import com.judy.secureprojecthub.repository.UserRoleRepository;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class RelationGraphQLController {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuditLogRepository auditLogRepository;

    public RelationGraphQLController(
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository,
            AuditLogRepository auditLogRepository) {
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @SchemaMapping(typeName = "User", field = "roles")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<Role> roles(User user) {
        // TODO: Add UserRoleRepository#findByUserId(Long userId) for this to compile.
        return userRoleRepository.findById(user.getId()).stream()
                .map(UserRole::getRole)
                .toList();
    }

    @SchemaMapping(typeName = "User", field = "auditLogs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> auditLogs(User user) {
        // TODO: Add AuditLogRepository#findByUserId(Long userId) for this to compile.
        return auditLogRepository.findByUserId(user.getId());
    }

    @SchemaMapping(typeName = "Role", field = "permissions")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<Permission> permissions(Role role) {
        // TODO: Add RolePermissionRepository#findByRoleId(Long roleId) for this to compile.
        return rolePermissionRepository.findById(role.getId()).stream()
                .map(RolePermission::getPermission)
                .toList();
    }
}
