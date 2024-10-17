package com.example.SpringApi.DatabaseModels.CentralDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "UserCarrierPermissionMapping")
public class UserCarrierPermissionMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserCarrierPermissionMappingId", nullable = false)
    private long userCarrierPermissionMappingId;

    @Column(name = "UserId", nullable = false)
    private long userId;

    @Column(name = "CarrierId", nullable = false)
    private long carrierId;

    @Column(name = "PermissionId", nullable = false)
    private long permissionId;

    // Tracking Fields
    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "AuditUserId")
    private Integer auditUserId;
}
