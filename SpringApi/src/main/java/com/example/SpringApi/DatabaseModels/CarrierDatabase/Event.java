package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;


@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "Event")
public class Event {
    // Event details
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EventId", nullable = false)
    private Long eventId;

    @Column(name = "EventName", nullable = false)
    private String eventName;

    @Column(name = "DescriptionHtml", nullable = false)
    private String descriptionHtml;

    @Column(name = "EventType")
    private String eventType;

    @Column(name = "PriorityStatus")
    private String priorityStatus;

    // Event date, time, and location
    @Column(name = "StartDateTime", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "EndDateTime", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "TimeZone", nullable = false)
    private String timeZone;

    @Column(name = "Location", nullable = false)
    private String location;

    // Event misc
    @Column(name = "Color")
    private String color;

    @Column(name = "ColorLabel")
    private String colorLabel;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "CreatedByUserId", nullable = false)
    private Long createdByUserId;

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