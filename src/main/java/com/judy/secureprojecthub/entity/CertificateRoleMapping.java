package com.judy.secureprojecthub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "certificate_role_mapping", uniqueConstraints = {
    @UniqueConstraint(columnNames = "certificate_cn")
})
public class CertificateRoleMapping {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "certificate_cn", nullable = false, unique = true, length = 255)
    private String certificateCN;
    
    @Column(name = "certificate_serial", length = 255)
    private String certificateSerial;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "override_role_id", nullable = false)
    private Role overrideRole;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public CertificateRoleMapping() {}
    
    public CertificateRoleMapping(String certificateCN, Role overrideRole) {
        this.certificateCN = certificateCN;
        this.overrideRole = overrideRole;
    }
    
    public CertificateRoleMapping(String certificateCN, String certificateSerial, Role overrideRole) {
        this.certificateCN = certificateCN;
        this.certificateSerial = certificateSerial;
        this.overrideRole = overrideRole;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCertificateCN() {
        return certificateCN;
    }
    
    public void setCertificateCN(String certificateCN) {
        this.certificateCN = certificateCN;
    }
    
    public String getCertificateSerial() {
        return certificateSerial;
    }
    
    public void setCertificateSerial(String certificateSerial) {
        this.certificateSerial = certificateSerial;
    }
    
    public Role getOverrideRole() {
        return overrideRole;
    }
    
    public void setOverrideRole(Role overrideRole) {
        this.overrideRole = overrideRole;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "CertificateRoleMapping{" +
                "id=" + id +
                ", certificateCN='" + certificateCN + '\'' +
                ", overrideRole=" + overrideRole +
                ", createdAt=" + createdAt +
                '}';
    }
}
