package com.example.ems.settings.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_user_backup_codes", indexes = {
    @Index(name = "idx_backup_codes_email", columnList = "user_email")
})
public class UserBackupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "hashed_code", nullable = false)
    private String hashedCode;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public UserBackupCode() {}

    public UserBackupCode(String userEmail, String hashedCode, boolean used, LocalDateTime createdAt) {
        this.userEmail = userEmail;
        this.hashedCode = hashedCode;
        this.used = used;
        this.createdAt = createdAt;
    }

    public UserBackupCode(String userEmail, String hashedCode, boolean used, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.userEmail = userEmail;
        this.hashedCode = hashedCode;
        this.used = used;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getHashedCode() { return hashedCode; }
    public void setHashedCode(String hashedCode) { this.hashedCode = hashedCode; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
