package com.judy.secureprojecthub.controller;

import com.judy.secureprojecthub.dto.TokenRequestDto;
import com.judy.secureprojecthub.dto.TokenResponseDto;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Date;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {
    private static final long EXPIRATION_TIME = 86400000; // 1 day in ms

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final SecretKey jwtSecretKey;

    public AuthController(AuthenticationManager authenticationManager, UserDetailsService userDetailsService, SecretKey jwtSecretKey) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtSecretKey = jwtSecretKey;
    }

    @PostMapping("/token")
    @Operation(summary = "Generate OAuth2 JWT Token", 
               description = "Authenticates user with username and password, returns a JWT token if credentials are valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token generated successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @SecurityRequirement(name = "")  // No security requirement for this endpoint
    public ResponseEntity<?> generateToken(@RequestBody TokenRequestDto request) {
        try {
            String username = request.getUsername();
            String password = request.getPassword();
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body("Username and password are required");
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
            TokenResponseDto response = new TokenResponseDto(token);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating token: " + e.getMessage());
        }
    }
}
