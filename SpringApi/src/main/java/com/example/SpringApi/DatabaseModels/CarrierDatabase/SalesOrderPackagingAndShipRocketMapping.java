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
@Table(name = "SalesOrderPackagingAndShipRocketMapping")
public class SalesOrderPackagingAndShipRocketMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SalesOrderPackagingMappingId", nullable = false)
    private long salesOrderPackagingMappingId;

    @Column(name = "ProductIds", nullable = false)
    private String productIds;

    // ShipRocket Columns
    @Column(name = "ShipRocketShipmentId", nullable = false)
    private Long shipRocketShipmentId;

    @Column(name = "ShipRocketOrderId", nullable = false)
    private Long shipRocketOrderId;

    @Column(name = "ShipRocketGeneratedAWB", nullable = false)
    private String shipRocketGeneratedAWB;

    @Column(name = "ShipRocketCourierId", nullable = false)
    private String shippingCourierId;

    @Column(name = "ShipRocketPickupTokenNumber", nullable = false)
    private String shipRocketPickupTokenNumber;

    @Column(name = "ShipRocketManifest", nullable = false)
    private String shipRocketManifest;

    @Column(name = "ShipRocketInvoice", nullable = false)
    private String shipRocketInvoice;

    @Column(name = "ShipRocketPrintInvoice", nullable = false)
    private String shipRocketPrintInvoice;

    @Column(name = "ShipRocketLabel", nullable = false)
    private String shipRocketLabel;

    // Fk's
    @Column(name = "SalesOrderId", nullable = false)
    private long salesOrderId;

    @JoinColumn(name = "PackageId", nullable = false)
    private long packageId;

    @Column(name = "PickupLocationId", nullable = false)
    private long pickupLocationId;

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