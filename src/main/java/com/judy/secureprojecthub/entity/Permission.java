package com.judy.secureprojecthub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Permission", description = "Permission entity representing an action on a resource")
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique permission identifier", example = "1")
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Unique permission name", example = "USER_READ", required = true)
    private String name;
    
    @Column(length = 500)
    @Schema(description = "Permission description", example = "Permission to read user data")
    private String description;
    
    @Column(nullable = false, length = 100)
    @Schema(description = "Resource this permission applies to", example = "USER", required = true)
    private String resource;
    
    @Column(nullable = false, length = 50)
    @Schema(description = "Action allowed on the resource", example = "READ", required = true)
    private String action;
    
    @Column(nullable = false)
    @Schema(description = "Permission creation timestamp", example = "2026-04-01T17:46:38.909261")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(nullable = false)
    @Schema(description = "Last update timestamp", example = "2026-04-01T17:46:38.972672")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @Version
    @Schema(hidden = true)
    private Long version;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getResource() {
        return resource;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}