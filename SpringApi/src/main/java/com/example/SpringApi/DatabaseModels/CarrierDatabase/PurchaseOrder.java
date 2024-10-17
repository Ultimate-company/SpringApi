package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "PurchaseOrder")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PurchaseOrderId", nullable = false)
    private Long purchaseOrderId;

    @Column(name = "ExpectedShipmentDate")
    private LocalDateTime expectedShipmentDate;

    @Column(name = "VendorNumber")
    private String vendorNumber;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "TermsConditionsHtml", nullable = false)
    private String termsConditionsHtml;

    @Column(name = "TermsConditionsMarkdown")
    private String termsConditionsMarkdown;

    @Column(name = "OrderReceipt")
    private String orderReceipt;

    @Column(name = "Approved", nullable = false)
    private boolean approved;

    @Column(name = "SalesOrderId")
    private Long salesOrderId;

    // Fk's
    @Column(name = "ApprovedByUserId")
    private Long approvedByUserId;

    @Column(name = "AssignedLeadId", nullable = false)
    private Long assignedLeadId;

    @Column(name = "CreatedByUserId", nullable = false, updatable = false)
    private Long createdByUserId;

    @Column(name = "PurchaseOrderAddressId", nullable = false)
    private Long purchaseOrderAddressId;

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
