package com.judy.secureprojecthub.controller;

import com.judy.secureprojecthub.entity.UserRole;
import com.judy.secureprojecthub.repository.UserRoleRepository;
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
@RequestMapping("/api/user-roles")
@Tag(name = "User Roles", description = "User-Role assignment management endpoints")
public class UserRoleController {
    @Autowired
    private UserRoleRepository userRoleRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all user-role assignments", description = "Retrieve a list of all user-role assignments in the system (requires ADMIN or USER role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignments retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public List<UserRole> getAllUserRoles() {
        return userRoleRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get user-role assignment by ID", description = "Retrieve a specific user-role assignment by its ID (requires ADMIN or USER role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignment found"),
        @ApiResponse(responseCode = "404", description = "User-role assignment not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
    })
    public ResponseEntity<UserRole> getUserRoleById(@PathVariable Long id) {
        Optional<UserRole> userRole = userRoleRepository.findById(id);
        return userRole.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new user-role assignment", description = "Assign a role to a user (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid user-role data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can assign roles")
    })
    public UserRole createUserRole(@RequestBody UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update user-role assignment", description = "Update an existing user-role assignment (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignment updated successfully"),
        @ApiResponse(responseCode = "404", description = "User-role assignment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid user-role data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can update role assignments")
    })
    public ResponseEntity<UserRole> updateUserRole(@PathVariable Long id, @RequestBody UserRole userRoleDetails) {
        return userRoleRepository.findById(id)
                .map(userRole -> {
                    return ResponseEntity.ok(userRoleRepository.save(userRole));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user-role assignment", description = "Remove a role assignment from a user (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User-role assignment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User-role assignment not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can delete role assignments")
    })
    public ResponseEntity<Void> deleteUserRole(@PathVariable Long id) {
        return userRoleRepository.findById(id)
                .map(userRole -> {
                    userRoleRepository.delete(userRole);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
