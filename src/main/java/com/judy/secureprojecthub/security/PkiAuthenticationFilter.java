package com.judy.secureprojecthub.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Filter that extracts X.509 client certificates from incoming requests
 * and creates Spring Security authentication tokens.
 * 
 * This filter works alongside JWT authentication to support PKI/mTLS authentication.
 */
public class PkiAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PkiAuthenticationFilter.class);

    private final PkiCertificateValidator certificateValidator;
    private final CertificateRoleService certificateRoleService;
    private final boolean validateExpiry;

    public PkiAuthenticationFilter(
            PkiCertificateValidator certificateValidator,
            CertificateRoleService certificateRoleService,
            boolean validateExpiry) {
        this.certificateValidator = certificateValidator;
        this.certificateRoleService = certificateRoleService;
        this.validateExpiry = validateExpiry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extract client certificate from request
            // Try multiple attribute names for compatibility
            X509Certificate[] certificates = (X509Certificate[]) request
                    .getAttribute("jakarta.servlet.request.X509Certificate");
            
            if (certificates == null) {
                // Fallback to javax.servlet.request.X509Certificate (for older Tomcat versions)
                certificates = (X509Certificate[]) request
                        .getAttribute("javax.servlet.request.X509Certificate");
            }
            
            log.debug("Looking for certificates. Found: {}", certificates != null ? certificates.length + " certificate(s)" : "none");

            if (certificates != null && certificates.length > 0) {
                X509Certificate clientCert = certificates[0];

                log.debug("Client certificate found: {}", certificateValidator.getCertificateInfo(clientCert));

                // Validate the certificate
                if (certificateValidator.validateCertificate(clientCert, validateExpiry)) {
                    // Extract certificate information
                    String commonName = certificateValidator.extractCommonName(clientCert);
                    String organization = certificateValidator.extractOrganization(clientCert);
                    String serialNumber = certificateValidator.getSerialNumber(clientCert);

                    log.debug("Certificate validation successful for CN={}, O={}, Serial={}",
                            commonName, organization, serialNumber);

                    // Create authentication token
                    // Use the Common Name as the principal
                    String principal = commonName != null ? commonName : "pki-user-" + serialNumber;

                    // Resolve role: database override → OU-based default → fallback to PKI_USER
                    String resolvedRole = certificateRoleService.resolveCertificateRole(clientCert);
                    
                    // Grant authorities to PKI-authenticated clients
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    
                    // Always grant base PKI_USER role
                    authorities.add(new SimpleGrantedAuthority("ROLE_PKI_USER"));
                    
                    // Grant the resolved role (could be ADMIN, USER, VIEWER, or PKI_USER)
                    authorities.add(new SimpleGrantedAuthority(resolvedRole));

                    // Add organization-based role if available
                    if (organization != null && !organization.isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + organization.toUpperCase().replaceAll("[^A-Z0-9]", "_")));
                    }

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,  // credentials
                            authorities
                    );

                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    // Store authentication details in request for later use
                    request.setAttribute("pki-certificate", clientCert);
                    request.setAttribute("pki-principal", principal);
                    request.setAttribute("pki-organization", organization);
                    request.setAttribute("pki-serial", serialNumber);

                    log.info("PKI authentication successful for principal: {}", principal);
                } else {
                    log.warn("Certificate validation failed: {}", certificateValidator.getCertificateInfo(clientCert));
                }
            } else {
                log.debug("No client certificate found in request");
            }
        } catch (Exception e) {
            log.error("Error processing PKI certificate filter: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
