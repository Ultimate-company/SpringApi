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
@Table(name = "Address")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AddressId", nullable = false)
    private long addressId;

    @Column(name = "Line1", nullable = false)
    private String line1;

    @Column(name = "Line2")
    private String line2;

    @Column(name = "Landmark")
    private String landmark;

    @Column(name = "State", nullable = false)
    private String state;

    @Column(name = "City", nullable = false)
    private String city;

    @Column(name = "ZipCode", nullable = false)
    private String zipCode;

    @Column(name = "NameOnAddress", nullable = false)
    private String nameOnAddress;

    @Column(name = "PhoneOnAddress", nullable = false)
    private String phoneOnAddress;

    @Column(name = "AddressLabel")
    private String addressLabel;

    @Column(name = "EmailAtAddress")
    private String emailAtAddress;

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

    // navigations
    @Column(name = "UserId", unique = true) // each user can have only one primary address
    private Long userId;
}