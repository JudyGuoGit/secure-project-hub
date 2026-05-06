package com.judy.secureprojecthub.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.judy.secureprojecthub.dto.TokenResponseDto;

import io.jsonwebtoken.Jwts;

@Service
public class JwtTokenService {

    private static final long EXPIRATION_TIME = 86400000L;

    private final AuthenticationManager authenticationManager;
    private final SecretKey jwtSecretKey;

    public JwtTokenService(AuthenticationManager authenticationManager,
                           SecretKey jwtSecretKey) {
        this.authenticationManager = authenticationManager;
        this.jwtSecretKey = jwtSecretKey;
    }

    public TokenResponseDto generateToken(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Username and password are required");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(jwtSecretKey)
                .compact();

        return new TokenResponseDto(token);
    }
}
