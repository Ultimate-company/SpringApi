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
@Table(name = "SupportComments")
public class SupportComments {
    @Id
    @Column(name = "CommentId", nullable = false)
    private String commentId;

    @Column(name = "RawCommentADF", nullable = false)
    private String rawCommentADF;

    // mapping fields
    @Column(name = "UserId")
    private Long userId;

    @Column(name = "TicketId", nullable = false)
    private String ticketId;

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
