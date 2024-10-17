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
@Table(name = "Support")
public class Support {
    @Id
    @Column(name = "TicketId", nullable = false)
    private String ticketId;

    @Column(name = "RawSupportDetails", nullable = false)
    private String rawSupportDetails;

    @Column(name = "Status", nullable = false)
    private String status;

    // mapping fields
    @Column(name = "UserId", nullable = false)
    private long userId;

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