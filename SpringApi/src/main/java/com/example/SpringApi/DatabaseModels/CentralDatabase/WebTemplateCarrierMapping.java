package com.example.SpringApi.DatabaseModels.CentralDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Accessors(chain = true)
@Table(name = "WebTemplateCarrierMapping")
public class WebTemplateCarrierMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long webTemplateCarrierMappingId;

    @Column(nullable = false, unique = true)
    private String wildCard;

    @Column(nullable = false, unique = true)
    private String apiAccessKey;

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

    // mapping Fields
    @Column(nullable = false)
    private Long webTemplateId;

    @Column(nullable = false)
    private Long carrierId;
}