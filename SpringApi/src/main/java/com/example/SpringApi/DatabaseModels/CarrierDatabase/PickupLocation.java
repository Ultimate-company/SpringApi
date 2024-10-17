package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "PickupLocation")
public class PickupLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PickupLocationId", nullable = false)
    private long pickupLocationId;

    @Column(name = "AddressNickName", nullable = false)
    private String addressNickName;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "PickupLocationAddressId", nullable = false)
    private long pickupLocationAddressId;

    @Column(name = "ShipRocketPickupLocationId", nullable = false)
    private long shipRocketPickupLocationId;

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