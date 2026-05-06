package com.judy.secureprojecthub.controller.restapi;

import com.judy.secureprojecthub.dto.TokenRequestDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import com.judy.secureprojecthub.service.JwtTokenService;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "Authentication and token management endpoints")
public class AuthController {
    private static final long EXPIRATION_TIME = 86400000; // 1 day in ms

    private final JwtTokenService jwtTokenService;

    public AuthController(JwtTokenService jwtTokenService) {
       this.jwtTokenService = jwtTokenService;
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
            return ResponseEntity.ok(
            		jwtTokenService.generateToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating token: " + e.getMessage());
        }
    }   
}
