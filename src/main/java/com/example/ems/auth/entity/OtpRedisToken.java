package com.example.ems.auth.entity;

import java.io.Serializable;

public class OtpRedisToken implements Serializable {
    private static final long serialVersionUID = 1L;

    private String otpHash;
    private int attemptCount;
    private boolean verified;
    private String createdAt; // Stored as ISO-8601 string

    public OtpRedisToken() {}

    public OtpRedisToken(String otpHash, int attemptCount, boolean verified, String createdAt) {
        this.otpHash = otpHash;
        this.attemptCount = attemptCount;
        this.verified = verified;
        this.createdAt = createdAt;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
