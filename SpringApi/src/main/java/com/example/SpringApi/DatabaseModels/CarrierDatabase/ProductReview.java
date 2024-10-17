package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ProductReview")
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewId", nullable = false)
    private Long reviewId;

    @Column(name = "Ratings", nullable = false)
    private double ratings;

    @Column(name = "Score", nullable = false)
    private int score;

    @Column(name = "IsDeleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "Review", nullable = false)
    private String review;

    // Fk's
    @Column(name = "UserId", nullable = false)
    private long userId;

    @Column(name = "ProductId", nullable = false)
    private long productId;

    @Column(name = "ParentId")
    private Long parentId;

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