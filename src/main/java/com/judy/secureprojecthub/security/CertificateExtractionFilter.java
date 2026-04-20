package com.judy.secureprojecthub.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.cert.X509Certificate;

/**
 * Filter that extracts X.509 client certificates from the TLS/SSL session
 * and makes them available to the servlet through request attributes.
 * 
 * This is CRITICAL for PKI/mTLS authentication because Spring Boot/Tomcat 
 * doesn't automatically expose client certificates through the standard servlet attribute.
 * 
 * This filter must run FIRST, before any authentication filters.
 */
@Component
@ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
@Order(Ordered.HIGHEST_PRECEDENCE)  // Run this filter first, before security filters
public class CertificateExtractionFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CertificateExtractionFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        log.debug("=== CertificateExtractionFilter ===");
        log.debug("Path: {}", request.getRequestURI());
        log.debug("HTTPS: {}", request.isSecure());
        log.debug("Remote Port: {}", request.getRemotePort());
        
        // Log all available attributes to debug
        java.util.Enumeration<String> attrNames = request.getAttributeNames();
        java.util.List<String> attrs = new java.util.ArrayList<>();
        while (attrNames.hasMoreElements()) {
            attrs.add(attrNames.nextElement());
        }
        log.debug("Available attributes: {}", attrs);

        // Try multiple methods to extract the client certificate
        X509Certificate[] certificates = null;

        // Method 1: Check if already set by Tomcat (standard Servlet API)
        try {
            Object attr = request.getAttribute("jakarta.servlet.request.X509Certificate");
            if (attr instanceof X509Certificate[]) {
                certificates = (X509Certificate[]) attr;
                log.debug("✓ Found certificate via jakarta.servlet.request.X509Certificate");
            }
        } catch (Exception e) {
            log.debug("Method 1 failed: {}", e.getMessage());
        }

        // Method 2: Try legacy javax attribute
        if (certificates == null) {
            try {
                Object attr = request.getAttribute("javax.servlet.request.X509Certificate");
                if (attr instanceof X509Certificate[]) {
                    certificates = (X509Certificate[]) attr;
                    log.debug("✓ Found certificate via javax.servlet.request.X509Certificate");
                }
            } catch (Exception e) {
                log.debug("Method 2 failed: {}", e.getMessage());
            }
        }

        // Method 3: Try to extract from SSL session via Tomcat internal APIs
        if (certificates == null) {
            try {
                // Tomcat stores SSL session in request attributes
                Object sslSession = request.getAttribute("javax.servlet.request.ssl_session");
                log.debug("SSL Session attribute: {}", sslSession);
                
                if (sslSession instanceof javax.net.ssl.SSLSession) {
                    javax.net.ssl.SSLSession session = (javax.net.ssl.SSLSession) sslSession;
                    try {
                        java.security.cert.Certificate[] peerCerts = session.getPeerCertificates();
                        if (peerCerts != null && peerCerts.length > 0) {
                            X509Certificate[] x509Certs = new X509Certificate[peerCerts.length];
                            for (int i = 0; i < peerCerts.length; i++) {
                                if (peerCerts[i] instanceof X509Certificate) {
                                    x509Certs[i] = (X509Certificate) peerCerts[i];
                                }
                            }
                            certificates = x509Certs;
                            log.debug("✓ Extracted {} certificate(s) from SSL session", certificates.length);
                        }
                    } catch (javax.net.ssl.SSLPeerUnverifiedException e) {
                        log.debug("No peer certificates in SSL session");
                    }
                }
            } catch (Exception e) {
                log.debug("Method 3 failed: {}", e.getMessage());
            }
        }

        // If we found certificates, ensure they're available via standard attributes
        if (certificates != null && certificates.length > 0) {
            request.setAttribute("jakarta.servlet.request.X509Certificate", certificates);
            request.setAttribute("javax.servlet.request.X509Certificate", certificates);
            log.debug("✓ Client certificate(s) exposed to servlet via request attributes");
        } else {
            log.debug("⚠ No client certificate found in request (HTTPS: {}, RemotePort: {})", 
                request.isSecure(), request.getRemotePort());
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}
