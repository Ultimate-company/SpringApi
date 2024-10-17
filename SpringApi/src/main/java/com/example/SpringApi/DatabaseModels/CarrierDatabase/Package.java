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
@Table(name = "Package")
public class Package {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PackageId", nullable = false)
    private long packageId;

    @Column(name = "LInches", nullable = false)
    private int length;

    @Column(name = "BInches", nullable = false)
    private int breadth;

    @Column(name = "HInches", nullable = false)
    private int height;

    @Column(name = "Quantity", nullable = false)
    private int quantity;

    @Column(name = "PricePerQuantity", nullable = false)
    private double pricePerQuantity;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    // Logging fields
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
