package com.judy.secureprojecthub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_roles", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "role_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserRole", description = "User-Role assignment mapping")
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique assignment identifier", example = "1")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "The user assigned to a role")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @Schema(description = "The role assigned to a user")
    private Role role;
    
    @Column(nullable = false)
    @Schema(description = "When the role was assigned", example = "2026-04-01T17:46:38.909261")
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    @Column(length = 255)
    @Schema(description = "Reason for role assignment", example = "Initial admin setup")
    private String reason;
    
    @Version
    private Long version;
}
