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
    @Operation(summary = "Get all user-role assignments", description = "Retrieve a list of all user-role assignments in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignments retrieved successfully"), 
    })
    public List<UserRole> getAllUserRoles() {
        return userRoleRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user-role assignment by ID", description = "Retrieve a specific user-role assignment by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignment found"), 
        @ApiResponse(responseCode = "404", description = "User-role assignment not found")
    })
    public ResponseEntity<UserRole> getUserRoleById(@PathVariable Long id) {
        Optional<UserRole> userRole = userRoleRepository.findById(id);
        return userRole.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new user-role assignment", description = "Assign a role to a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignment created successfully"), 
        @ApiResponse(responseCode = "400", description = "Invalid user-role data")
    })
    public UserRole createUserRole(@RequestBody UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user-role assignment", description = "Update an existing user-role assignment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User-role assignment updated successfully"), 
        @ApiResponse(responseCode = "404", description = "User-role assignment not found"),
        @ApiResponse(responseCode = "400", description = "Invalid user-role data")
    })
    public ResponseEntity<UserRole> updateUserRole(@PathVariable Long id, @RequestBody UserRole userRoleDetails) {
        return userRoleRepository.findById(id)
                .map(userRole -> {
                    // Set fields as needed
                    return ResponseEntity.ok(userRoleRepository.save(userRole));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user-role assignment", description = "Remove a role assignment from a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User-role assignment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User-role assignment not found")
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
