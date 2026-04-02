package com.judy.secureprojecthub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for token request - login credentials
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "TokenRequest", description = "Token request payload - login credentials")
public class TokenRequestDto {
    
    @Schema(description = "Username for authentication", example = "admin", required = true)
    private String username;
    
    @Schema(description = "Password for authentication", example = "admin", required = true, format = "password")
    private String password;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}















    
}
