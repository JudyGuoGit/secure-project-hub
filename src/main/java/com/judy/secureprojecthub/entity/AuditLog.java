package com.judy.secureprojecthub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "AuditLog", description = "AuditLog entity tracking user actions for compliance and security")
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique audit log identifier", example = "1")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Schema(description = "The user who performed the action")
    private User user;
    
    @Column(nullable = false, length = 100)
    @Schema(description = "Action name", example = "USER_LOGIN", required = true)
    private String action;
    
    @Column(nullable = false, length = 50)
    @Schema(description = "Type of action", example = "AUTHENTICATION", required = true)
    private String actionType;
    
    @Column(length = 500)
    @Schema(description = "Detailed description of the action", example = "User logged in successfully")
    private String description;
    
    @Column(length = 50)
    @Schema(description = "Status of the action", example = "SUCCESS")
    private String status;
    
    @Column(length = 45)
    @Schema(description = "IP address from which the action was performed", example = "192.168.1.1")
    private String ipAddress;
    
    @Column(length = 500)
    @Schema(description = "User agent string from the request", example = "Mozilla/5.0...")
    private String userAgent;
    
    @Column(nullable = false, updatable = false)
    @Schema(description = "When this action was logged", example = "2026-04-01T17:46:38.909261")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column
    @Schema(description = "Additional JSON details about the action", example = "{\"method\":\"POST\",\"endpoint\":\"/api/login\"}")
    private String details;
    
    @Version
    @Schema(hidden = true)
    private Long version;

	public void setCreatedAt(LocalDateTime now) {
		// TODO Auto-generated method stub
		
	}

	public Object getCreatedAt() {
		// TODO Auto-generated method stub
		return null;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
