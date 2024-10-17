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
@Table(name = "`Lead`")
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LeadId", nullable = false)
    private long leadId;

    @Column(name = "AnnualRevenue")
    private String annualRevenue;

    @Column(name = "Company")
    private String company;

    @Column(name = "CompanySize")
    private Integer companySize;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "Fax")
    private String fax;

    @Column(name = "LastName", nullable = false)
    private String lastName;

    @Column(name = "LeadStatus", nullable = false)
    private String leadStatus;

    @Column(name = "Phone", nullable = false)
    private String phone;

    @Column(name = "Title")
    private String title;

    @Column(name = "Website")
    private String website;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    // Fk's
    @Column(name = "AddressId", nullable = false)
    private long addressId;

    @Column(name = "CreatedById", nullable = false)
    private long createdById;

    @Column(name = "AssignedAgentId")
    private Long assignedAgentId;


    // Tracking Fields
    @CreationTimestamp
    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Notes")
    private String notes;

    @Column(name = "AuditUserId")
    private Long auditUserId;
}