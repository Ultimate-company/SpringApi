package com.example.SpringApi.DatabaseModels.CentralDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "Carrier")
public class Carrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CarrierId", nullable = false, updatable = false)
    private long carrierId;

    @Column(name = "Name", unique = true, nullable = false, updatable = false)
    private String name;

    @Column(name = "Description", nullable = false, updatable = false)
    private String description;

    @Column(name = "DatabaseName", nullable = false, updatable = false)
    private String databaseName;

    @Column(name = "SendGridApiKey")
    private String sendgridApikey;

    @Column(name = "SendGridEmailAddress")
    private String sendgridEmailAddress;

    @Column(name = "IsDeleted", nullable = false, updatable = false)
    private boolean isDeleted;

    @Column(name = "Image", nullable = false)
    private String image;

    @Column(name = "Website", nullable = false)
    private String website;

    @Column(name = "SendgridSenderName", nullable = false)
    private String sendgridSenderName;

    @Column(name = "RazorpayApikey")
    private String razorpayApikey;

    @Column(name = "RazorpayApisecret")
    private String razorpayApiSecret;

    @Column(name = "ShiprocketEmail")
    private String shipRocketEmail;

    @Column(name = "ShiprocketPassword")
    private String shipRocketPassword;

    @Column(name = "JiraUserName")
    private String jiraUserName;

    @Column(name = "JiraPassword")
    private String jiraPassword;

    @Column(name = "JiraProjectUrl")
    private String jiraProjectUrl;

    @Column(name = "JiraProjectKey")
    private String jiraProjectKey;

    @Column(name = "IssueTypes")
    private String issueTypes;

    @Column(name = "BoxDeveloperToken")
    private String boxDeveloperToken;

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