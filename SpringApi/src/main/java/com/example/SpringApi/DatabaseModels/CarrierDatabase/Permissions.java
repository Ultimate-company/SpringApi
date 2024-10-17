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
@Table(name = "Permissions")
public class Permissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PermissionId", nullable = false)
    private long permissionId;

    @Column(name = "UserPermissions")
    private String userPermissions;

    @Column(name = "UserlogPermissions")
    private String userLogPermissions;

    @Column(name = "GroupsPermissions")
    private String groupsPermissions;

    @Column(name = "MessagesPermissions")
    private String messagesPermissions;

    @Column(name = "PromosPermissions")
    private String promosPermissions;

    @Column(name = "AddressPermissions")
    private String addressPermissions;

    @Column(name = "PickuplocationPermissions")
    private String pickupLocationPermissions;

    @Column(name = "OrdersPermissions")
    private String ordersPermissions;

    @Column(name = "PaymentsPermissions")
    private String paymentsPermissions;

    @Column(name = "EventsPermissions")
    private String eventsPermissions;

    @Column(name = "ProductsPermissions")
    private String productsPermissions;

    @Column(name = "SupportPermissions")
    private String supportPermissions;

    @Column(name = "ApikeyPermissions")
    private String apiKeyPermissions;

    @Column(name = "LeadsPermissions")
    private String leadsPermissions;

    @Column(name = "PurchaseorderPermissions")
    private String purchaseOrderPermissions;

    @Column(name = "SalesorderPermissions")
    private String salesOrderPermissions;

    @Column(name = "WebtemplatePermissions")
    private String webTemplatePermissions;

    @Column(name = "PackagePermissions")
    private String packagePermissions;

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
