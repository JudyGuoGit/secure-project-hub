package com.judy.secureprojecthub.controller;

import com.judy.secureprojecthub.entity.CertificateRoleMapping;
import com.judy.secureprojecthub.security.CertificateRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/certificate-roles")
@Tag(name = "Certificate Role Management", description = "Manage certificate-to-role mappings for PKI authentication")
public class CertificateRoleController {
    
    @Autowired
    private CertificateRoleService certificateRoleService;
    
    /**
     * Add or update a certificate-to-role mapping (admin escalation)
     * 
     * @param certificateCN the certificate Common Name
     * @param roleName the role to assign
     * @return the created/updated mapping
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add certificate role mapping", 
               description = "Create or update a mapping for privilege escalation (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mapping created/updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid role name"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can manage mappings")
    })
    public ResponseEntity<?> addCertificateRoleMapping(
            @RequestParam String certificateCN,
            @RequestParam String roleName) {
        try {
            CertificateRoleMapping mapping = certificateRoleService.addCertificateRoleOverride(certificateCN, roleName);
            return ResponseEntity.ok(mapping);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Remove a certificate-to-role mapping
     * 
     * @param certificateCN the certificate Common Name
     * @return success message
     */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove certificate role mapping",
               description = "Delete a certificate-to-role mapping (requires ADMIN role)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Mapping removed successfully"),
        @ApiResponse(responseCode = "403", description = "Forbidden - only ADMIN can manage mappings")
    })
    public ResponseEntity<?> removeCertificateRoleMapping(@RequestParam String certificateCN) {
        try {
            certificateRoleService.removeCertificateRoleOverride(certificateCN);
            return ResponseEntity.ok("Certificate role mapping removed for CN: " + certificateCN);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
