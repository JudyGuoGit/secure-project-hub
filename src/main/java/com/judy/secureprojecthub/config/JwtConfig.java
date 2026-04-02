package com.judy.secureprojecthub.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

/**
 * Centralized JWT configuration to ensure consistent secret key usage
 * across token generation and validation.
 */
@Configuration
public class JwtConfig {
    
    // 32-byte (256-bit) secret key for HS256 - MUST be at least 32 bytes
    private static final String SECRET_KEY_STRING = "this_is_a_secret_key_of_32_bytes!!";
    
    @Bean
    public SecretKey jwtSecretKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    }
}
