package com.judy.secureprojecthub.controller;

import com.judy.secureprojecthub.security.PkiCertificateValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for PKI/mTLS authentication endpoints.
 * 
 * This controller is only enabled when pki.enabled=true in application.yml
 * All endpoints require PKI_USER role (authenticated via client certificate).
 */
@RestController
@RequestMapping("/api/pki")
@Tag(name = "PKI/mTLS", description = "PKI and mTLS authentication endpoints")
@ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
@SecurityRequirement(name = "x509")
public class PkiController {

    private static final Logger log = LoggerFactory.getLogger(PkiController.class);

    @Autowired
    private PkiCertificateValidator certificateValidator;

    /**
     * Health check endpoint for PKI authentication.
     * Verifies that the client certificate is valid and authenticated.
     *
     * @param request The HTTP request containing certificate information
     * @return Certificate details and authentication status
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('PKI_USER')")
    @Operation(
        summary = "PKI Health Check",
        description = "Verify PKI authentication is working and get certificate details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "PKI authentication successful"),
        @ApiResponse(responseCode = "403", description = "PKI authentication failed - invalid or missing certificate"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - no valid certificate provided")
    })
    public ResponseEntity<Map<String, Object>> pkiHealth(HttpServletRequest request) {
        log.info("PKI health check endpoint called");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "PKI authenticated");
        response.put("message", "Client certificate authentication successful");

        // Extract certificate information from request attributes
        X509Certificate certificate = (X509Certificate) request.getAttribute("pki-certificate");
        String principal = (String) request.getAttribute("pki-principal");
        String organization = (String) request.getAttribute("pki-organization");
        String serial = (String) request.getAttribute("pki-serial");

        response.put("principal", principal);
        response.put("organization", organization);
        response.put("serialNumber", serial);

        if (certificate != null) {
            response.put("certificateInfo", certificateValidator.getCertificateInfo(certificate));
            response.put("commonName", certificateValidator.extractCommonName(certificate));
            response.put("country", certificateValidator.extractCountry(certificate));
            response.put("notBefore", certificateValidator.getNotBefore(certificate));
            response.put("notAfter", certificateValidator.getNotAfter(certificate));
            response.put("isValid", certificateValidator.isCertificateValid(certificate));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get detailed certificate information for the authenticated client.
     *
     * @param request The HTTP request containing certificate information
     * @return Detailed certificate information
     */
    @GetMapping("/certificate-info")
    @PreAuthorize("hasRole('PKI_USER')")
    @Operation(
        summary = "Get Certificate Info",
        description = "Retrieve detailed information about the client certificate"
    )
    @ApiResponse(responseCode = "200", description = "Certificate information retrieved")
    public ResponseEntity<Map<String, Object>> getCertificateInfo(HttpServletRequest request) {
        log.info("Get certificate info endpoint called");

        X509Certificate certificate = (X509Certificate) request.getAttribute("pki-certificate");
        if (certificate == null) {
            log.warn("No certificate found in request");
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> certInfo = new HashMap<>();
        certInfo.put("subjectDN", certificateValidator.extractSubjectDN(certificate));
        certInfo.put("issuerDN", certificateValidator.extractIssuerDN(certificate));
        certInfo.put("commonName", certificateValidator.extractCommonName(certificate));
        certInfo.put("organization", certificateValidator.extractOrganization(certificate));
        certInfo.put("country", certificateValidator.extractCountry(certificate));
        certInfo.put("serialNumber", certificateValidator.getSerialNumber(certificate));
        certInfo.put("notBefore", certificateValidator.getNotBefore(certificate));
        certInfo.put("notAfter", certificateValidator.getNotAfter(certificate));
        certInfo.put("isValid", certificateValidator.isCertificateValid(certificate));
        certInfo.put("signatureAlgorithm", certificate.getSigAlgName());

        return ResponseEntity.ok(certInfo);
    }

    /**
     * Verify certificate validity and get expiration information.
     *
     * @param request The HTTP request containing certificate information
     * @return Certificate validity status
     */
    @GetMapping("/verify")
    @PreAuthorize("hasRole('PKI_USER')")
    @Operation(
        summary = "Verify Certificate",
        description = "Verify the validity and expiration status of the client certificate"
    )
    @ApiResponse(responseCode = "200", description = "Certificate verification completed")
    public ResponseEntity<Map<String, Object>> verifyCertificate(HttpServletRequest request) {
        log.info("Verify certificate endpoint called");

        X509Certificate certificate = (X509Certificate) request.getAttribute("pki-certificate");
        if (certificate == null) {
            log.warn("No certificate found in request");
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> verification = new HashMap<>();
        verification.put("isValid", certificateValidator.isCertificateValid(certificate));
        verification.put("notBefore", certificateValidator.getNotBefore(certificate));
        verification.put("notAfter", certificateValidator.getNotAfter(certificate));
        verification.put("serialNumber", certificateValidator.getSerialNumber(certificate));
        verification.put("commonName", certificateValidator.extractCommonName(certificate));

        return ResponseEntity.ok(verification);
    }

    /**
     * Protected resource accessible only via PKI authentication.
     * Demonstrates that PKI and OAuth2 can coexist with different endpoints.
     *
     * @param request The HTTP request containing certificate information
     * @return Protected data
     */
    @GetMapping("/secure-data")
    @PreAuthorize("hasRole('PKI_USER')")
    @Operation(
        summary = "Access Secure Data",
        description = "Access a protected resource that requires PKI authentication"
    )
    @ApiResponse(responseCode = "200", description = "Access granted")
    public ResponseEntity<Map<String, Object>> getSecureData(HttpServletRequest request) {
        log.info("Secure data endpoint called via PKI");

        String principal = (String) request.getAttribute("pki-principal");

        Map<String, Object> response = new HashMap<>();
        response.put("message", "This data is protected by PKI authentication");
        response.put("accessedBy", principal);
        response.put("timestamp", System.currentTimeMillis());
        response.put("note", "This endpoint is only accessible with a valid client certificate (mTLS)");

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint that demonstrates dual authentication support.
     * Can be accessed by either PKI or OAuth2 authentication.
     *
     * @param request The HTTP request
     * @return Response indicating authentication method used
     */
    @GetMapping("/auth-method")
    @PreAuthorize("hasAnyRole('PKI_USER', 'USER', 'ADMIN')")
    @Operation(
        summary = "Get Authentication Method",
        description = "Determine which authentication method was used (PKI or OAuth2)"
    )
    @ApiResponse(responseCode = "200", description = "Authentication method identified")
    public ResponseEntity<Map<String, Object>> getAuthMethod(HttpServletRequest request) {
        log.info("Auth method endpoint called");

        Map<String, Object> response = new HashMap<>();

        // Check if PKI certificate is present
        X509Certificate certificate = (X509Certificate) request.getAttribute("pki-certificate");
        if (certificate != null) {
            response.put("authMethod", "PKI/mTLS");
            response.put("principal", request.getAttribute("pki-principal"));
            response.put("certificate", certificateValidator.getCertificateInfo(certificate));
        } else {
            response.put("authMethod", "OAuth2/JWT");
            response.put("principal", request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "unknown");
        }

        return ResponseEntity.ok(response);
    }
}
