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
@Table(name = "SalesOrdersProductQuantityMap")
public class SalesOrdersProductQuantityMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SalesOrdersProductQuantityMapId", nullable = false)
    private long salesOrdersProductQuantityMapId;

    @Column(name = "Quantity", nullable = false)
    private int quantity;

    @Column(name = "PricePerQuantityPerProduct", nullable = false)
    private double pricePerQuantityPerProduct;

    // Fk's
    @Column(name = "ProductId", nullable = false)
    private long productId;

    @Column(name = "SalesOrderId", nullable = false)
    private long salesOrderId;

    // Logging Fields
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