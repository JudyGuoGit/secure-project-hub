package com.judy.secureprojecthub.config;

import com.judy.secureprojecthub.security.JwtTokenFilter;
import com.judy.secureprojecthub.security.PkiAuthenticationFilter;
import com.judy.secureprojecthub.security.PkiCertificateValidator;
import com.judy.secureprojecthub.security.CertificateRoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;

import javax.crypto.SecretKey;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private final DatabaseUserDetailsService userDetailsService;

    public SecurityConfig(DatabaseUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter(SecretKey jwtSecretKey) {
        return new JwtTokenFilter(jwtSecretKey);
    }

    /**
     * Creates the PKI authentication filter (when pki.enabled=true).
     */
    @Bean
    @ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
    public PkiAuthenticationFilter pkiAuthenticationFilter(
            PkiCertificateValidator certificateValidator,
            CertificateRoleService certificateRoleService) {
        log.info("Initializing PKI Authentication Filter with certificate role mapping");
        return new PkiAuthenticationFilter(certificateValidator, certificateRoleService, true);
    }

    /**
     * Loads the server keystore for SSL/TLS (when pki.enabled=true).
     */
    @Bean
    @ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
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
     * Loads the truststore for SSL/TLS (when pki.enabled=true).
     */
    @Bean
    @ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
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
     * Creates the SSLContext with both key and trust managers configured (when pki.enabled=true).
     */
    @Bean
    @ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
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
     * PKI certificate validator bean (when pki.enabled=true).
     */
    @Bean
    @ConditionalOnProperty(name = "pki.enabled", havingValue = "true")
    public PkiCertificateValidator pkiCertificateValidator() {
        log.info("Initializing PKI Certificate Validator");
        return new PkiCertificateValidator();
    }
    
  

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenFilter jwtTokenFilter, 
    		ObjectProvider<PkiAuthenticationFilter>  pkiFilterProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/token").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        PkiAuthenticationFilter pkiFilter = pkiFilterProvider.getIfAvailable();
        if (pkiFilter != null) {
            http.addFilterBefore(pkiFilter, BasicAuthenticationFilter.class);
            http.addFilterBefore(jwtTokenFilter, BasicAuthenticationFilter.class);
        } else {
            http.addFilterBefore(jwtTokenFilter, BasicAuthenticationFilter.class);
        }

        return http.build();
    }
}
