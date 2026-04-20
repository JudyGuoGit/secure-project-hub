package com.judy.secureprojecthub.security;

import com.judy.secureprojecthub.entity.CertificateRoleMapping;
import com.judy.secureprojecthub.entity.Role;
import com.judy.secureprojecthub.repository.CertificateRoleMappingRepository;
import com.judy.secureprojecthub.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Service for resolving PKI client roles based on certificate attributes.
 * 
 * Strategy:
 * 1. Check database for certificate CN → role override (admin escalation)
 * 2. If not found, determine default role from certificate OU
 * 3. Fallback to default PKI_USER role
 */
@Service
public class CertificateRoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateRoleService.class);
    
    private final CertificateRoleMappingRepository certificateRoleMappingRepository;
    private final RoleRepository roleRepository;
    private final PkiCertificateValidator certificateValidator;
    
    public CertificateRoleService(
            CertificateRoleMappingRepository certificateRoleMappingRepository,
            RoleRepository roleRepository,
            PkiCertificateValidator certificateValidator) {
        this.certificateRoleMappingRepository = certificateRoleMappingRepository;
        this.roleRepository = roleRepository;
        this.certificateValidator = certificateValidator;
    }
    
    /**
     * Resolve the role for a PKI-authenticated client
     * 
     * @param clientCert the X.509 certificate
     * @return the resolved role name (e.g., "ROLE_ADMIN", "ROLE_USER", "ROLE_PKI_USER")
     */
    public String resolveCertificateRole(X509Certificate clientCert) {
        String cn = certificateValidator.extractCommonName(clientCert);
        String ou = certificateValidator.extractOrganizationalUnit(clientCert);
        String serialNumber = certificateValidator.getSerialNumber(clientCert);
        
        logger.debug("Resolving role for certificate CN={}, OU={}, Serial={}", cn, ou, serialNumber);
        
        // Step 1: Check for database override (admin escalation)
        if (cn != null) {
            Optional<CertificateRoleMapping> mapping = 
                certificateRoleMappingRepository.findByCertificateCN(cn);
            
            if (mapping.isPresent()) {
                String roleName = "ROLE_" + mapping.get().getOverrideRole().getName().toUpperCase();
                logger.info("Certificate CN={} has database override: {}", cn, roleName);
                return roleName;
            }
        }
        
        // Step 2: Determine default role from certificate OU
        String defaultRole = mapOUToRole(ou);
        logger.info("Certificate CN={} assigned default role from OU={}: {}", cn, ou, defaultRole);
        
        return defaultRole;
    }
    
    /**
     * Map certificate Organizational Unit to a role
     * 
     * @param ou the organizational unit from certificate DN
     * @return the role name (e.g., "ROLE_ADMIN", "ROLE_USER")
     */
    public String mapOUToRole(String ou) {
        if (ou == null || ou.trim().isEmpty()) {
            return "ROLE_PKI_USER"; // Default fallback
        }
        
        String ouUpperCase = ou.toUpperCase().trim();
        
        // Map common OU values to roles
        switch (ouUpperCase) {
            case "ADMIN":
            case "ADMINISTRATORS":
            case "ADMIN_TEAM":
                return "ROLE_ADMIN";
                
            case "OPERATIONS":
            case "OPS":
            case "OPS_TEAM":
                return "ROLE_USER";
                
            case "READONLY":
            case "READ_ONLY":
            case "VIEWER":
                return "ROLE_VIEWER";
                
            default:
                // If OU doesn't match known patterns, treat as regular user
                logger.debug("Unknown OU '{}', assigning ROLE_PKI_USER", ou);
                return "ROLE_PKI_USER";
        }
    }
    
    /**
     * Create or update a certificate-to-role mapping
     * Use this for admin escalation (e.g., granting ADMIN role to specific client cert)
     * 
     * @param certificateCN the certificate Common Name
     * @param roleName the role to assign
     * @return the created/updated mapping
     */
    public CertificateRoleMapping addCertificateRoleOverride(String certificateCN, String roleName) {
        Optional<Role> role = roleRepository.findByNameIgnoreCase(roleName);
        
        if (role.isEmpty()) {
            throw new IllegalArgumentException("Role not found: " + roleName);
        }
        
        Optional<CertificateRoleMapping> existing = 
            certificateRoleMappingRepository.findByCertificateCN(certificateCN);
        
        CertificateRoleMapping mapping;
        if (existing.isPresent()) {
            mapping = existing.get();
            logger.info("Updating certificate role mapping for CN={}", certificateCN);
        } else {
            mapping = new CertificateRoleMapping();
            logger.info("Creating new certificate role mapping for CN={}", certificateCN);
        }
        
        mapping.setCertificateCN(certificateCN);
        mapping.setOverrideRole(role.get());
        
        return certificateRoleMappingRepository.save(mapping);
    }
    
    /**
     * Remove a certificate-to-role mapping
     * 
     * @param certificateCN the certificate Common Name
     */
    public void removeCertificateRoleOverride(String certificateCN) {
        Optional<CertificateRoleMapping> mapping = 
            certificateRoleMappingRepository.findByCertificateCN(certificateCN);
        
        if (mapping.isPresent()) {
            certificateRoleMappingRepository.delete(mapping.get());
            logger.info("Removed certificate role mapping for CN={}", certificateCN);
        }
    }
}
