package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "UserGridPreference")
public class UserGridPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserGridPreferenceId")
    private Long userGridPreferenceId;

    @Column(name = "Density", nullable = false)
    private String density;

    @Lob
    @Column(name = "VisibilityModel")
    private String visibilityModel;

    @Column(name = "RowsPerPage")
    private int rowsPerPage;

    @Column(name = "GridId")
    private int gridId;

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

    // mapping fields
    @Column(name = "UserId", nullable = false)
    private long userId;
}
