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
    @Operation(summary = "Get all role-permission assignments", description = "Retrieve a list of all role-permission assignments in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignments retrieved successfully"), 
    })
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role-permission assignment by ID", description = "Retrieve a specific role-permission assignment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignment found"), 
        @ApiResponse(responseCode = "404", description = "Role-permission assignment not found")
    })
    public ResponseEntity<RolePermission> getRolePermissionById(@PathVariable Long id) {
        Optional<RolePermission> rolePermission = rolePermissionRepository.findById(id);
        return rolePermission.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new role-permission assignment", description = "Grant a permission to a role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignment created successfully"), 
        @ApiResponse(responseCode = "400", description = "Invalid role-permission data")
    })
    public RolePermission createRolePermission(@RequestBody RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role-permission assignment", description = "Update an existing role-permission assignment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role-permission assignment updated successfully"), 
        @ApiResponse(responseCode = "404", description = "Role-permission assignment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid role-permission data")
    })
    public ResponseEntity<RolePermission> updateRolePermission(@PathVariable Long id, @RequestBody RolePermission rolePermissionDetails) {
        return rolePermissionRepository.findById(id)
                .map(rolePermission -> {
                    // Set fields as needed
                    return ResponseEntity.ok(rolePermissionRepository.save(rolePermission));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role-permission assignment", description = "Revoke a permission from a role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role-permission assignment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Role-permission assignment not found")
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
