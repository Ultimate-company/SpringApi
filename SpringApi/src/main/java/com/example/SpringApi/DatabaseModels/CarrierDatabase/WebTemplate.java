package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "WebTemplate")
public class WebTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WebTemplateId", nullable = false)
    private long webTemplateId;

    @Column(name = "SortOptions", nullable = false)
    private String sortOptions;

    @Column(name = "SelectedProducts", nullable = false)
    private String selectedProducts;

    @Column(name = "FilterOptions", nullable = false)
    private String filterOptions;

    @Column(name = "StateCitiesMapping", nullable = false)
    private String stateCitiesMapping;

    @Column(name = "AcceptedPaymentOptions", nullable = false)
    private String acceptedPaymentOptions;

    @Column(name = "Url", nullable = false, unique = true)
    private String url;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    // Mappings
    @Column(name = "CardHeaderFontStyleId", nullable = false)
    private long cardHeaderFontStyleId;

    @Column(name = "CardSubTextFontStyleId", nullable = false)
    private long cardSubTextFontStyleId;

    @Column(name = "HeaderFontStyleId", nullable = false)
    private long headerFontStyleId;

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
}