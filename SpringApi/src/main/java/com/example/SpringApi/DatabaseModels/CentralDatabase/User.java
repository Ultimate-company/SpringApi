package com.example.SpringApi.DatabaseModels.CentralDatabase;
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
@Table(name = "User")
@SecondaryTable(name = "UserGroupsUsersMap", pkJoinColumns = @PrimaryKeyJoinColumn(name = "UserId"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId", nullable = false)
    private long userId;

    @Column(name = "LoginName", unique = true, nullable = false)
    private String loginName;

    @Column(name = "Password", nullable = false)
    private String password;

    @Column(name = "Salt", nullable = false)
    private String salt;

    @Column(name = "FirstName", nullable = false)
    private String firstName;

    @Column(name = "LastName", nullable = false)
    private String lastName;

    @Column(name = "Phone", nullable = false)
    private String phone;

    @Column(name = "DatePasswordChanges")
    private Date datePasswordChanges;

    @Column(name = "LoginAttempts", nullable = false)
    private int loginAttempts;

    @Column(name = "Role", nullable = false)
    private String role;

    @Column(name = "IsDeleted", nullable = false)
    private boolean deleted;

    @Column(name = "Locked", nullable = false)
    private boolean locked;

    @Column(name = "EmailConfirmed", nullable = false)
    private boolean emailConfirmed;

    @Column(name = "Token", nullable = false)
    private String token;

    @Column(name = "Avatar")
    private String avatar;

    @Column(name = "Dob", nullable = false)
    private Date dob;

    @Column(name = "IsGuest", nullable = false)
    private boolean guest;

    @Column(name = "LockedAttempts", nullable = false)
    private int lockedAttempts;

    @Column(name = "ApiKey", nullable = false)
    private String apiKey;

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
