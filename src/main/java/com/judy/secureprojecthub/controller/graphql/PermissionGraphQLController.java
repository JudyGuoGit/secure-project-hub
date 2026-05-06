package com.judy.secureprojecthub.controller.graphql;

import com.judy.secureprojecthub.entity.Permission;
import com.judy.secureprojecthub.graphql.payload.CreatePermissionInput;
import com.judy.secureprojecthub.graphql.payload.UpdatePermissionInput;
import com.judy.secureprojecthub.repository.PermissionRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PermissionGraphQLController {

    private final PermissionRepository permissionRepository;

    public PermissionGraphQLController(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public List<Permission> permissions() {
        return permissionRepository.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public Permission permission(@Argument Long id) {
        return permissionRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Permission createPermission(@Argument CreatePermissionInput input) {
        Permission permission = new Permission();
        permission.setName(input.name());
        permission.setDescription(input.description());
        permission.setResource(input.resource());
        permission.setAction(input.action());
        return permissionRepository.save(permission);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Permission updatePermission(@Argument Long id, @Argument UpdatePermissionInput input) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + id));

        if (input.name() != null) permission.setName(input.name());
        if (input.description() != null) permission.setDescription(input.description());
        if (input.resource() != null) permission.setResource(input.resource());
        if (input.action() != null) permission.setAction(input.action());

        return permissionRepository.save(permission);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deletePermission(@Argument Long id) {
        if (!permissionRepository.existsById(id)) {
            return false;
        }
        permissionRepository.deleteById(id);
        return true;
    }
}
