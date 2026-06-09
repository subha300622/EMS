package com.example.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who requested the reset. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** BCrypt-hashed 6-digit OTP. */
    @Column(nullable = false)
    private String otpHash;

    /** UUID reset token — set only after OTP is successfully verified. */
    @Column(unique = true)
    private String resetToken;

    /** OTP expiry — 10 minutes from creation. */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /** Reset token expiry — 5 minutes after OTP is verified. */
    private LocalDateTime resetTokenExpiresAt;

    /** Whether the OTP has been verified. */
    private boolean verified = false;

    /** Number of incorrect OTP attempts (max 5). */
    private int attemptCount = 0;

    /** Creation time — used for 60-second resend cooldown check. */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
