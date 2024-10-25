package com.example.SpringApi.DatabaseModels.CentralDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "GoogleCred")
public class GoogleCred {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GoogleCredId")
    private long googleCredId;

    @Column(name = "Type", nullable = false, columnDefinition = "TEXT")
    private String type;

    @Column(name = "ProjectId", nullable = false, columnDefinition = "TEXT")
    private String projectId;

    @Column(name = "PrivateKeyId", nullable = false, columnDefinition = "TEXT")
    private String privateKeyId;

    @Column(name = "Privatekey", nullable = false, columnDefinition = "LONGTEXT")
    private String privateKey;

    @Column(name = "ClientEmail", nullable = false, columnDefinition = "TEXT")
    private String clientEmail;

    @Column(name = "ClientId", nullable = false, columnDefinition = "TEXT")
    private String clientId;

    @Column(name = "AuthUri", nullable = false, columnDefinition = "TEXT")
    private String authUri;

    @Column(name = "TokenUri", nullable = false, columnDefinition = "TEXT")
    private String tokenUri;

    @Column(name = "AuthProviderx509CertUrl", nullable = false, columnDefinition = "TEXT")
    private String authProviderx509CertUrl;

    @Column(name = "Clientx509CertUrl", nullable = false, columnDefinition = "TEXT")
    private String clientx509CertUrl;

    @Column(name = "UniverseDomain", nullable = false, columnDefinition = "TEXT")
    private String universeDomain;

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
