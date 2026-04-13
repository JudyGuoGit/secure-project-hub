package com.judy.secureprojecthub.config;

import com.judy.secureprojecthub.security.PkiAuthenticationFilter;
import com.judy.secureprojecthub.security.PkiCertificateValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Configuration for PKI/mTLS support.
 * 
 * This configuration enables mutual TLS (mTLS) authentication alongside OAuth2 JWT.
 * It sets up SSL/TLS with client certificate validation.
 */
@Configuration
@ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
public class PkiSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(PkiSecurityConfig.class);

    /**
     * Creates the PKI authentication filter.
     *
     * @param certificateValidator The certificate validator bean
     * @return The configured PKI authentication filter
     */
    @Bean
    public PkiAuthenticationFilter pkiAuthenticationFilter(PkiCertificateValidator certificateValidator) {
        log.info("Initializing PKI Authentication Filter");
        return new PkiAuthenticationFilter(certificateValidator, true);
    }

    /**
     * Loads the server keystore for SSL/TLS.
     * This contains the server's private key and certificate.
     *
     * @return The loaded KeyStore
     * @throws Exception if keystore loading fails
     */
    @Bean
    public KeyStore serverKeyStore() throws Exception {
        log.info("Loading server keystore for PKI");
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        
        try (InputStream keystoreFile = new ClassPathResource("keystore.p12").getInputStream()) {
            keyStore.load(keystoreFile, "changeit".toCharArray());
            log.info("✓ Server keystore loaded successfully");
        }
        
        return keyStore;
    }

    /**
     * Loads the truststore for SSL/TLS.
     * This contains the CA certificate(s) for validating client certificates.
     *
     * @return The loaded KeyStore
     * @throws Exception if truststore loading fails
     */
    @Bean
    public KeyStore trustStore() throws Exception {
        log.info("Loading truststore for PKI");
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        
        try (InputStream truststoreFile = new ClassPathResource("truststore.p12").getInputStream()) {
            trustStore.load(truststoreFile, "changeit".toCharArray());
            log.info("✓ Truststore loaded successfully");
        }
        
        return trustStore;
    }

    /**
     * Creates the SSLContext with both key and trust managers configured.
     * This is used for mTLS communication.
     *
     * @param serverKeyStore The server keystore
     * @param trustStore The truststore
     * @return The configured SSLContext
     * @throws Exception if SSLContext creation fails
     */
    @Bean
    public SSLContext sslContext(KeyStore serverKeyStore, KeyStore trustStore) throws Exception {
        log.info("Initializing SSLContext for PKI/mTLS");
        
        // Initialize KeyManagerFactory with server keystore
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(serverKeyStore, "changeit".toCharArray());
        log.debug("✓ KeyManagerFactory initialized");

        // Initialize TrustManagerFactory with truststore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        log.debug("✓ TrustManagerFactory initialized");

        // Create SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        log.info("✓ SSLContext initialized for TLS");

        return sslContext;
    }

    /**
     * The PKI certificate validator bean for validating client certificates.
     *
     * @return A new PkiCertificateValidator instance
     */
    @Bean
    public PkiCertificateValidator pkiCertificateValidator() {
        log.info("Initializing PKI Certificate Validator");
        return new PkiCertificateValidator();
    }
}
