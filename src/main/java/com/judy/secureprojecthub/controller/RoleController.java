package com.judy.secureprojecthub.controller;

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

import com.judy.secureprojecthub.entity.Role;
import com.judy.secureprojecthub.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Role management endpoints")
public class RoleController {
    @Autowired
    private RoleRepository roleRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all roles", description = "Retrieve a list of all roles in the system (requires ADMIN or USER role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role by its ID (requires ADMIN or USER role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role found"), 
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new role", description = "Create a new role in the system (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role created successfully"), 
        @ApiResponse(responseCode = "400", description = "Invalid role data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can create roles")
    })
    public Role createRole(@RequestBody Role role) {
        return roleRepository.save(role);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update role", description = "Update an existing role's information (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully"), 
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "400", description = "Invalid role data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can update roles")
    })
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        return roleRepository.findById(id)
                .map(role -> {
                    role.setName(roleDetails.getName());
                    role.setDescription(roleDetails.getDescription());
                    role.setUpdatedAt(roleDetails.getUpdatedAt());
                    return ResponseEntity.ok(roleRepository.save(role));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete role", description = "Delete a role from the system (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can delete roles")
    })
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        return roleRepository.findById(id)
                .map(role -> {
                    roleRepository.delete(role);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}