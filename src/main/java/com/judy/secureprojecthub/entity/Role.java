package com.judy.secureprojecthub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "Role", description = "Role entity representing a security role in the system")
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique role identifier", example = "1")
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Unique role name", example = "ADMIN", required = true)
    private String name;
    
    @Column(length = 500)
    @Schema(description = "Role description", example = "Administrator role with full permissions")
    private String description;
    
    @Column(nullable = false)
    @Schema(description = "Role creation timestamp", example = "2026-04-01T17:46:38.909261")
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
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}