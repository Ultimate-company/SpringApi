package com.example.SpringApi.DatabaseModels.CarrierDatabase;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "Message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MessageId", nullable = false)
    private Long messageId;

    @Column(name = "Title", nullable = false)
    private String title;

    @Column(name = "PublishDate")
    private Date publishDate;

    @Column(name = "Description")
    private String description;

    @Column(name = "DescriptionMarkDown")
    private String descriptionMarkDown;

    @Column(name = "DescriptionHtml", nullable = false)
    private String descriptionHtml;

    @Column(name = "SendAsEmail", nullable = false)
    private boolean sendAsEmail;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "SendgridEmailBatchId")
    private String sendgridEmailBatchId;

    @Column(name = "CreatedByUserId")
    private long createdByUserId;

    @Column(name = "IsUpdated", nullable = false)
    private boolean updated;

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


