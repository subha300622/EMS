package com.example.ems.settings.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_preferences", indexes = {
    @Index(name = "idx_notif_pref_email", columnList = "user_email")
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String category;

    private Boolean email = true;
    private Boolean push = true;
    private Boolean sms = false;

    public NotificationPreference() {}

    public NotificationPreference(String userEmail, String category, Boolean email, Boolean push, Boolean sms) {
        this.userEmail = userEmail;
        this.category = category;
        this.email = email;
        this.push = push;
        this.sms = sms;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Boolean getEmail() { return email; }
    public void setEmail(Boolean email) { this.email = email; }

    public Boolean getPush() { return push; }
    public void setPush(Boolean push) { this.push = push; }

    public Boolean getSms() { return sms; }
    public void setSms(Boolean sms) { this.sms = sms; }
}
