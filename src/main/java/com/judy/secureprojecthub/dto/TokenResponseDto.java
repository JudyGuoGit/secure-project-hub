package com.judy.secureprojecthub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token response - JWT bearer token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TokenResponse", description = "Token response payload - JWT bearer token")
public class TokenResponseDto {
    
    @Schema(description = "JWT Bearer token to use in Authorization header. Valid for 24 hours.", 
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjpbeyJhdXRob3JpdHkiOiJST0xFX0FETUlOIn1dLCJpYXQ6MTY0MTk4NzY3MywiZXhwIjoxNjQyMDc0MDczfQ.token_signature",
            required = true)
    private String token;
}
