package com.judy.secureprojecthub.repository;

import com.judy.secureprojecthub.entity.CertificateRoleMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRoleMappingRepository extends JpaRepository<CertificateRoleMapping, Long> {
    
    /**
     * Find a certificate role mapping by Common Name (CN)
     * @param certificateCN the CN from the certificate Subject DN
     * @return Optional containing the mapping if found
     */
    Optional<CertificateRoleMapping> findByCertificateCN(String certificateCN);
    
    /**
     * Find a certificate role mapping by both CN and Serial (more secure)
     * @param certificateCN the CN from the certificate
     * @param certificateSerial the serial number from the certificate
     * @return Optional containing the mapping if found
     */
    Optional<CertificateRoleMapping> findByCertificateCNAndCertificateSerial(
            String certificateCN, String certificateSerial);
}
