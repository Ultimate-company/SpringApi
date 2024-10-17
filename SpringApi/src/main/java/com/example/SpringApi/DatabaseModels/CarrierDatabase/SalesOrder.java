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
@Table(name = "SalesOrder") // No underscores, plural form
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SalesOrderId", nullable = false)
    private long salesOrderId;

    @Column(name = "TermsAndConditionsMarkdown")
    private String termsAndConditionsMarkdown;

    @Column(name = "TermsAndConditionsHtml", nullable = false)
    private String termsAndConditionsHtml;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "OptedForInsurance", nullable = false)
    private boolean optedForInsurance;

    @Column(name = "SalesOrderStatus", nullable = false)
    private int salesOrderStatus;

    // Fk's
    @Column(name = "PaymentId", nullable = false)
    private long paymentId;

    @JoinColumn(name = "BillingAddressId", nullable = false)
    private long billingAddressId;

    @JoinColumn(name = "ShippingAddressId", nullable = false)
    private long shippingAddressId;

    @JoinColumn(name = "PurchaseOrderId", nullable = false)
    private long purchaseOrderId;

    @Column(name = "CreatedByUserId", nullable = false, updatable = false)
    private long createdByUserId;

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