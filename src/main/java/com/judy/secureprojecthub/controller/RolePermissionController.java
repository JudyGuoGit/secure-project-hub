package com.judy.secureprojecthub.controller;

import com.judy.secureprojecthub.entity.RolePermission;
import com.judy.secureprojecthub.repository.RolePermissionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/role-permissions")
@Tag(name = "Role Permissions", description = "Role-Permission assignment management endpoints")
public class RolePermissionController {
    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all role-permission assignments", description = "Retrieve a list of all role-permission assignments in the system (requires ADMIN or USER role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get role-permission assignment by ID", description = "Retrieve a specific role-permission assignment by its ID (requires ADMIN or USER role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignment found"),
        @ApiResponse(responseCode = "404", description = "Role-permission assignment not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<RolePermission> getRolePermissionById(@PathVariable Long id) {
        Optional<RolePermission> rolePermission = rolePermissionRepository.findById(id);
        return rolePermission.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new role-permission assignment", description = "Grant a permission to a role (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role-permission data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can grant permissions")
    })
    public RolePermission createRolePermission(@RequestBody RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update role-permission assignment", description = "Update an existing role-permission assignment (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignment updated successfully"),
        @ApiResponse(responseCode = "404", description = "Role-permission assignment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid role-permission data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can update permission assignments")
    })
    public ResponseEntity<RolePermission> updateRolePermission(@PathVariable Long id, @RequestBody RolePermission rolePermissionDetails) {
        return rolePermissionRepository.findById(id)
                .map(rolePermission -> {
                    return ResponseEntity.ok(rolePermissionRepository.save(rolePermission));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete role-permission assignment", description = "Revoke a permission from a role (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role-permission assignment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Role-permission assignment not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can revoke permissions")
    })
    public ResponseEntity<Void> deleteRolePermission(@PathVariable Long id) {
        return rolePermissionRepository.findById(id)
                .map(rolePermission -> {
                    rolePermissionRepository.delete(rolePermission);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
