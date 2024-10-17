package com.example.SpringApi.DatabaseModels.CentralDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "GoogleUser")
public class GoogleUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private long userId;

    @Column(name = "AccessToken", nullable = false)
    private String accessToken;

    @Column(name = "GoogleId", nullable = false)
    private String googleId;

    @Column(name = "ImageUrl", nullable = false)
    private String imageUrl;

    @Column(name = "TokenId", nullable = false)
    private String tokenId;

    @Column(name = "Email", nullable = false)
    private String email;

    @Column(name = "FamilyName", nullable = false)
    private String familyName;

    @Column(name = "GivenName", nullable = false)
    private String givenName;

    @Column(name = "Name")
    private String name;

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
