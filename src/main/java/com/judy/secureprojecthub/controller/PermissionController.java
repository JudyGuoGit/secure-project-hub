package com.judy.secureprojecthub.controller;

import com.judy.secureprojecthub.entity.Permission;
import com.judy.secureprojecthub.repository.PermissionRepository;
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
@RequestMapping("/api/permissions")
@Tag(name = "Permissions", description = "Permission management endpoints")
public class PermissionController {
    @Autowired
    private PermissionRepository permissionRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all permissions", description = "Retrieve a list of all permissions in the system (requires ADMIN or USER role)")
    @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get permission by ID", description = "Retrieve a specific permission by its ID (requires ADMIN or USER role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission found"),
        @ApiResponse(responseCode = "404", description = "Permission not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<Permission> getPermissionById(@PathVariable Long id) {
        Optional<Permission> permission = permissionRepository.findById(id);
        return permission.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new permission", description = "Create a new permission in the system (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid permission data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can create permissions")
    })
    public Permission createPermission(@RequestBody Permission permission) {
        return permissionRepository.save(permission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update permission", description = "Update an existing permission's information (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Permission updated successfully"),
        @ApiResponse(responseCode = "404", description = "Permission not found"),
        @ApiResponse(responseCode = "400", description = "Invalid permission data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can update permissions")
    })
    public ResponseEntity<Permission> updatePermission(@PathVariable Long id, @RequestBody Permission permissionDetails) {
        return permissionRepository.findById(id)
                .map(permission -> {
                    permission.setName(permissionDetails.getName());
                    permission.setDescription(permissionDetails.getDescription());
                    permission.setResource(permissionDetails.getResource());
                    permission.setAction(permissionDetails.getAction());
                    permission.setUpdatedAt(permissionDetails.getUpdatedAt());
                    return ResponseEntity.ok(permissionRepository.save(permission));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete permission", description = "Delete a permission from the system (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Permission deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Permission not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can delete permissions")
    })
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        return permissionRepository.findById(id)
                .map(permission -> {
                    permissionRepository.delete(permission);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}