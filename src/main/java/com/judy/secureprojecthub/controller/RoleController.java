package com.judy.secureprojecthub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "Get all roles", description = "Retrieve a list of all roles in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role found"), 
        @ApiResponse(responseCode = "404", description = "Role not found")
    })
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Optional<Role> role = roleRepository.findById(id);
        return role.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new role", description = "Create a new role in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role created successfully"), 
        @ApiResponse(responseCode = "400", description = "Invalid role data")
    })
    public Role createRole(@RequestBody Role role) {
        return roleRepository.save(role);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Update an existing role's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully"), 
        @ApiResponse(responseCode = "404", description = "Role not found"),
        @ApiResponse(responseCode = "400", description = "Invalid role data")
    })
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role roleDetails) {
        return roleRepository.findById(id)
                .map(role -> {
                    role.setName(roleDetails.getName());
                    role.setDescription(roleDetails.getDescription());
                    role.setUpdatedAt(roleDetails.getUpdatedAt());
                    // Do not update createdAt
                    return ResponseEntity.ok(roleRepository.save(role));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Delete a role from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Role not found")
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