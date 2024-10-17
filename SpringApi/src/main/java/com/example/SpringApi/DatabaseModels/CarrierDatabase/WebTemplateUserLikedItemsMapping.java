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
@Table(name = "WebTemplateUserLikedItemsMapping")
public class WebTemplateUserLikedItemsMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long webTemplateUserLikedItemsMappingId;

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
    @Column(nullable = false)
    private Long webTemplateId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;
}
