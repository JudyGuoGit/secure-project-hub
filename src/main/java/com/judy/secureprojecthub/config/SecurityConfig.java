package com.judy.secureprojecthub.config;

import com.judy.secureprojecthub.security.JwtTokenFilter;
import com.judy.secureprojecthub.security.PkiAuthenticationFilter;
import com.judy.secureprojecthub.security.PkiCertificateValidator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.beans.factory.ObjectProvider;

import javax.crypto.SecretKey;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
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

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtTokenFilter jwtTokenFilter, 
                                          ObjectProvider<PkiAuthenticationFilter> pkiFilterProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/token").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // REST API endpoints - accept both JWT/OAuth2 and PKI authentication
                // PKI-authenticated users get ROLE_PKI_USER which is allowed alongside other roles
                .requestMatchers("/api/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtTokenFilter, BasicAuthenticationFilter.class);
        
        // Add PKI authentication filter if it was created (pki.enabled=true)
        // This filter runs before BasicAuthenticationFilter and sets authentication for PKI clients
        pkiFilterProvider.ifAvailable(pkiFilter -> {
            try {
                http.addFilterBefore(pkiFilter, BasicAuthenticationFilter.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add PKI authentication filter", e);
            }
        });
        
        http.sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
        
        return http.build();
    }
}
