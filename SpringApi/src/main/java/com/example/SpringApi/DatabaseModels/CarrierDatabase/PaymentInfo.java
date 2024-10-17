package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.Column;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "PaymentInfo")
public class PaymentInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentId", nullable = false)
    private long paymentId;

    @Column(name = "Total", nullable = false)
    private double total;

    @Column(name = "Tax", nullable = false)
    private double tax;

    @Column(name = "ServiceFee", nullable = false)
    private double serviceFee;

    @Column(name = "PackagingFee", nullable = false)
    private double packagingFee;

    @Column(name = "Discount", nullable = false)
    private double discount;

    @Column(name = "Status", nullable = false)
    private int status;

    @Column(name = "Mode", nullable = false)
    private int mode;

    @Column(name = "SubTotal", nullable = false)
    private double subTotal;

    @Column(name = "DeliveryFee", nullable = false)
    private double deliveryFee;

    @Column(name = "PendingAmount", nullable = false)
    private double pendingAmount;

    // razor pay fields
    @Column(name = "RazorpayTransactionId")
    private String razorpayTransactionId;

    @Column(name = "RazorpayReceipt")
    private String razorpayReceipt;

    @Column(name = "RazorpayOrderId")
    private String razorpayOrderId;

    @Column(name = "RazorpayPaymentNotes")
    private String razorpayPaymentNotes;

    @Column(name = "RazorpaySignature")
    private String razorpaySignature;

    // Fk's
    @Column(name = "PromoId")
    private Long promoId;

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