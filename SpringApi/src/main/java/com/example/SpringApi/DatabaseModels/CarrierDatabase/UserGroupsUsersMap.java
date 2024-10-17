package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "UserGroupsUsersMap")
public class UserGroupsUsersMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserGroupMappingId", nullable = false)
    private long userGroupMappingId;

    // Tracking Fields
    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "AuditUserId")
    private Long auditUserId;

    // Mapping Fields
    @Column(name = "UserId", nullable = false)
    private long userId;

    @Column(name = "UserGroupId", nullable = false)
    private long userGroupId;
}