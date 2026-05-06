package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.entity.Permission;
import com.judy.secureprojecthub.entity.Role;
import com.judy.secureprojecthub.entity.RolePermission;
import com.judy.secureprojecthub.graphql.payload.CreateRolePermissionInput;
import com.judy.secureprojecthub.graphql.payload.UpdateRolePermissionInput;
import com.judy.secureprojecthub.repository.PermissionRepository;
import com.judy.secureprojecthub.repository.RolePermissionRepository;
import com.judy.secureprojecthub.repository.RoleRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class RolePermissionGraphQLController {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermissionGraphQLController(
            RolePermissionRepository rolePermissionRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<RolePermission> rolePermissions() {
        return rolePermissionRepository.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public RolePermission rolePermission(@Argument Long id) {
        return rolePermissionRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RolePermission createRolePermission(@Argument CreateRolePermissionInput input) {
        return grantPermissionToRole(input.roleId(), input.permissionId());
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RolePermission grantPermissionToRole(@Argument Long roleId, @Argument Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionId));

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermission.setGrantedAt(LocalDateTime.now());
        return rolePermissionRepository.save(rolePermission);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RolePermission updateRolePermission(@Argument Long id, @Argument UpdateRolePermissionInput input) {
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("RolePermission not found: " + id));

        if (input.roleId() != null) {
            rolePermission.setRole(roleRepository.findById(input.roleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + input.roleId())));
        }
        if (input.permissionId() != null) {
            rolePermission.setPermission(permissionRepository.findById(input.permissionId())
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + input.permissionId())));
        }

        return rolePermissionRepository.save(rolePermission);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteRolePermission(@Argument Long id) {
        if (!rolePermissionRepository.existsById(id)) {
            return false;
        }
        rolePermissionRepository.deleteById(id);
        return true;
    }
}
