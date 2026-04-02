package com.judy.secureprojecthub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "User", description = "User entity representing a user account in the system")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique user identifier", example = "1")
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Unique username for login", example = "admin", required = true)
    private String username;
    
    @Column(nullable = false, unique = true, length = 255)
    @Schema(description = "User email address", example = "admin@example.com", required = true)
    private String email;
    
    @Column(nullable = false, length = 255)
    @Schema(description = "BCrypt hashed password", example = "$2a$10$...", required = true)
    private String passwordHash;
    
    @Column(nullable = false)
    @Schema(description = "Whether user account is enabled", example = "true")
    private Boolean enabled = true;
    
    @Column(nullable = false)
    @Schema(description = "Whether user account is not expired", example = "true")
    private Boolean accountNonExpired = true;
    
    @Column(nullable = false)
    @Schema(description = "Whether user credentials are not expired", example = "true")
    private Boolean credentialsNonExpired = true;
    
    @Column(nullable = false)
    @Schema(description = "Whether user account is not locked", example = "true")
    private Boolean accountNonLocked = true;
    
    @Column(length = 255)
    @Schema(description = "User full name", example = "Admin User")
    private String fullName;
    
    @Column(length = 500)
    @Schema(description = "User biography or description", example = "Administrator account")
    private String bio;
    
    @Column(nullable = false, updatable = false)
    @Schema(description = "Account creation timestamp", example = "2026-04-01T17:46:38.909261")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    @Schema(description = "Last update timestamp", example = "2026-04-01T17:46:38.972672")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Column
    private LocalDateTime lastLoginAt;
    
    @Version
    private Long version;

    public String getUsername() {
        return username;
    }
    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public Boolean getEnabled() {
        return enabled;
    }
    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }
    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }
    public String getFullName() {
        return fullName;
    }
    public String getBio() {
        return bio;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
	public void setCreatedAt(LocalDateTime now) {
		// TODO Auto-generated method stub
		
	}
}