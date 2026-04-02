package com.judy.secureprojecthub.controller;

import com.judy.secureprojecthub.entity.AuditLog;
import com.judy.secureprojecthub.repository.AuditLogRepository;
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
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Audit log management endpoints for tracking user actions")
public class AuditLogController {
    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Get all audit logs", description = "Retrieve a list of all audit logs in the system")
    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully")
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by ID", description = "Retrieve a specific audit log by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit log found"),
        @ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        Optional<AuditLog> auditLog = auditLogRepository.findById(id);
        return auditLog.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new audit log", description = "Create a new audit log entry in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit log created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid audit log data")
    })
    public AuditLog createAuditLog(@RequestBody AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update audit log", description = "Update an existing audit log's information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Audit log updated successfully"),
        @ApiResponse(responseCode = "404", description = "Audit log not found"),
        @ApiResponse(responseCode = "400", description = "Invalid audit log data")
    })
    public ResponseEntity<AuditLog> updateAuditLog(@PathVariable Long id, @RequestBody AuditLog auditLogDetails) {
        return auditLogRepository.findById(id)
                .map(auditLog -> {
                    // Set fields as needed
                    return ResponseEntity.ok(auditLogRepository.save(auditLog));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete audit log", description = "Delete an audit log from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Audit log deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    public ResponseEntity<Void> deleteAuditLog(@PathVariable Long id) {
        return auditLogRepository.findById(id)
                .map(auditLog -> {
                    auditLogRepository.delete(auditLog);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
