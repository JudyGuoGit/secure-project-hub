package com.judy.secureprojecthub.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "role_permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"role_id", "permission_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RolePermission", description = "RolePermission entity mapping permissions to roles")
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique role-permission assignment identifier", example = "1")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @Schema(description = "The role assigned with this permission")
    private Role role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false)
    @Schema(description = "The permission granted to the role")
    private Permission permission;
    
    @Column(nullable = false)
    @Schema(description = "When the permission was granted to the role", example = "2026-04-01T17:46:38.909261")
    private LocalDateTime grantedAt = LocalDateTime.now();
    
    @Version
    @Schema(hidden = true)
    private Long version;
}
