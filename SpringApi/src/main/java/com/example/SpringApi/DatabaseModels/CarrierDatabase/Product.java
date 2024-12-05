package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Blob;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductId", nullable = false)
    private long productId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "DescriptionHtml", nullable = false)
    private String descriptionHtml;

    @Column(name = "Length")
    private Double length;

    @Column(name = "AvailableStock", nullable = false)
    private int availableStock;

    @Column(name = "Brand", nullable = false)
    private String brand;

    @Column(name = "Color")
    private String color;

    @Column(name = "ColorLabel", nullable = false)
    private String colorLabel;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "`Condition`", nullable = false)
    private int condition;

    @Column(name = "CountryOfManufacture", nullable = false)
    private String countryOfManufacture;

    @Column(name = "Model")
    private String model;

    @Column(name = "ItemModified", nullable = false)
    private boolean itemModified;

    @Column(name = "Upc")
    private String upc;

    @Column(name = "ModificationHtml")
    private String modificationHtml;

    @Column(name = "Price", nullable = false)
    private double price;

    @Column(name = "Discount", nullable = false)
    private double discount;

    @Column(name = "IsDiscountPercent", nullable = false)
    private boolean discountPercent;

    @Column(name = "ReturnsAllowed", nullable = false)
    private boolean returnsAllowed;

    @Column(name = "ItemAvailableFrom", nullable = false)
    private LocalDateTime itemAvailableFrom;

    @Column(name = "Breadth")
    private Double breadth;

    @Column(name = "Height")
    private Double height;

    @Column(name = "WeightKgs")
    private Double weightKgs;

    @Column(name = "PickupLocationId", nullable = false)
    private long pickupLocationId;

    //mapping Fields
    @Column(name = "CategoryId")
    private long categoryId;

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