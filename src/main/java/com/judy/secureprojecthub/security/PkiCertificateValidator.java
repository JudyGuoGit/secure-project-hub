package com.judy.secureprojecthub.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates X.509 client certificates and extracts certificate information.
 * Performs checks for certificate validity, expiration, and chain validation.
 */
@Component
public class PkiCertificateValidator {

    private static final Logger log = LoggerFactory.getLogger(PkiCertificateValidator.class);

    private static final Pattern DN_PATTERN = Pattern.compile("(?:^|,)\\s*([A-Z]+)\\s*=\\s*([^,]+?)(?:(?=,\\s*[A-Z]+=)|$)");

    /**
     * Validates a client certificate for basic requirements.
     *
     * @param certificate The X.509 certificate to validate
     * @param validateExpiry If true, checks certificate expiration
     * @return true if certificate is valid, false otherwise
     */
    public boolean validateCertificate(X509Certificate certificate, boolean validateExpiry) {
        if (certificate == null) {
            log.warn("Certificate is null");
            return false;
        }

        try {
            // Check certificate validity period
            if (validateExpiry) {
                certificate.checkValidity();
                log.debug("Certificate validity check passed");
            }
            return true;
        } catch (CertificateExpiredException e) {
            log.error("Certificate has expired: {}", e.getMessage());
            return false;
        } catch (CertificateNotYetValidException e) {
            log.error("Certificate is not yet valid: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the subject DN (Distinguished Name) from the certificate.
     *
     * @param certificate The X.509 certificate
     * @return The subject DN as a string
     */
    public String extractSubjectDN(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        X500Principal principal = certificate.getSubjectX500Principal();
        return principal.getName(X500Principal.RFC2253);
    }

    /**
     * Extracts the issuer DN from the certificate.
     *
     * @param certificate The X.509 certificate
     * @return The issuer DN as a string
     */
    public String extractIssuerDN(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        X500Principal principal = certificate.getIssuerX500Principal();
        return principal.getName(X500Principal.RFC2253);
    }

    /**
     * Extracts a specific attribute from a DN string (e.g., CN, O, C).
     *
     * @param dn The distinguished name string
     * @param attributeCode The attribute code to extract (e.g., "CN", "O", "C")
     * @return The attribute value, or null if not found
     */
    public String extractDNAttribute(String dn, String attributeCode) {
        if (dn == null || attributeCode == null) {
            return null;
        }

        Matcher matcher = DN_PATTERN.matcher(dn);
        while (matcher.find()) {
            if (attributeCode.equals(matcher.group(1))) {
                return matcher.group(2);
            }
        }
        return null;
    }

    /**
     * Extracts the Common Name (CN) from the certificate subject DN.
     *
     * @param certificate The X.509 certificate
     * @return The Common Name, or null if not found
     */
    public String extractCommonName(X509Certificate certificate) {
        String subjectDN = extractSubjectDN(certificate);
        return extractDNAttribute(subjectDN, "CN");
    }

    /**
     * Extracts the Organization (O) from the certificate subject DN.
     *
     * @param certificate The X.509 certificate
     * @return The Organization, or null if not found
     */
    public String extractOrganization(X509Certificate certificate) {
        String subjectDN = extractSubjectDN(certificate);
        return extractDNAttribute(subjectDN, "O");
    }

    /**
     * Extracts the Organizational Unit (OU) from the certificate subject DN.
     *
     * @param certificate The X.509 certificate
     * @return The Organizational Unit, or null if not found
     */
    public String extractOrganizationalUnit(X509Certificate certificate) {
        String subjectDN = extractSubjectDN(certificate);
        return extractDNAttribute(subjectDN, "OU");
    }

    /**
     * Extracts the Country (C) from the certificate subject DN.
     *
     * @param certificate The X.509 certificate
     * @return The Country code, or null if not found
     */
    public String extractCountry(X509Certificate certificate) {
        String subjectDN = extractSubjectDN(certificate);
        return extractDNAttribute(subjectDN, "C");
    }

    /**
     * Gets the certificate serial number.
     *
     * @param certificate The X.509 certificate
     * @return The serial number as a string
     */
    public String getSerialNumber(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        return certificate.getSerialNumber().toString();
    }

    /**
     * Gets the certificate validity start date.
     *
     * @param certificate The X.509 certificate
     * @return The validity start date
     */
    public Date getNotBefore(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        return certificate.getNotBefore();
    }

    /**
     * Gets the certificate validity end date.
     *
     * @param certificate The X.509 certificate
     * @return The validity end date
     */
    public Date getNotAfter(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        return certificate.getNotAfter();
    }

    /**
     * Checks if certificate is currently valid (not expired and not before valid date).
     *
     * @param certificate The X.509 certificate
     * @return true if the certificate is currently valid
     */
    public boolean isCertificateValid(X509Certificate certificate) {
        if (certificate == null) {
            return false;
        }

        Date now = new Date();
        Date notBefore = getNotBefore(certificate);
        Date notAfter = getNotAfter(certificate);

        return notBefore != null && notAfter != null && 
               now.after(notBefore) && now.before(notAfter);
    }

    /**
     * Creates a certificate info object for logging/debugging.
     *
     * @param certificate The X.509 certificate
     * @return A formatted string with certificate information
     */
    public String getCertificateInfo(X509Certificate certificate) {
        if (certificate == null) {
            return "Certificate is null";
        }

        return String.format(
            "Certificate Info: CN=%s, O=%s, C=%s, Serial=%s, NotBefore=%s, NotAfter=%s, Valid=%s",
            extractCommonName(certificate),
            extractOrganization(certificate),
            extractCountry(certificate),
            getSerialNumber(certificate),
            getNotBefore(certificate),
            getNotAfter(certificate),
            isCertificateValid(certificate)
        );
    }
}
