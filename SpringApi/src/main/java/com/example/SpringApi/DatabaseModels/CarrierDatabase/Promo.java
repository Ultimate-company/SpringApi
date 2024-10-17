package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Promo")
public class Promo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PromoId", nullable = false)
    private long promoId;

    @Column(name = "Description", nullable = false)
    private String description;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "IsPercent", nullable = false)
    private boolean percent;

    @Column(name = "DiscountValue", nullable = false)
    private double discountValue;

    @Column(name = "PromoCode", unique = true, nullable = false)
    private String promoCode;

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