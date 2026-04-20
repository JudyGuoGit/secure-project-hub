package com.judy.secureprojecthub.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat SSL/TLS Configuration for PKI/mTLS support in Spring Boot 4.x.
 * 
 * In Spring Boot 4.x, client certificate extraction is handled by:
 * 1. application.yml configuration (server.ssl.client-auth: WANT)
 * 2. CertificateExtractionFilter (extracts and exposes certificates to servlets)
 * 
 * This class serves as a marker for PKI configuration initialization.
 */
@Configuration
@ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
public class TomcatSslConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(TomcatSslConfiguration.class);

    public TomcatSslConfiguration() {
        log.info("✓ PKI/mTLS Configuration Initialized");
        log.info("  - Spring Boot 4.x SSL support active");
        log.info("  - CertificateExtractionFilter will expose client certificates");
        log.info("  - Configuration: server.ssl.client-auth=WANT");
    }
}