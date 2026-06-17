package com.example.ems.settings.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_settings", indexes = {
    @Index(name = "idx_emp_settings_email", columnList = "user_email")
})
public class EmployeeSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", unique = true, nullable = false)
    private String userEmail;

    // Appearance
    private String theme = "DARK";
    private String fontSize = "MEDIUM";
    private Boolean compactMode = false;

    // Language & Region
    private String language = "en";
    private String timezone = "Asia/Kolkata";
    private String dateFormat = "dd-MM-yyyy";
    private String timeFormat = "24_HOUR";

    // Security
    private Boolean mfaEnabled = true;
    private LocalDateTime passwordLastChanged;

    // Privacy
    private Boolean profileVisible = true;
    private Boolean showPhoneNumber = false;
    private Boolean showEmail = true;
    private Boolean showBirthday = false;

    // Notifications Timing
    private Boolean dailyDigestEnabled = true;
    private String digestTime = "09:00";
    private Boolean quietHoursEnabled = true;
    private String quietHoursFrom = "22:00";
    private String quietHoursTo = "07:00";

    public EmployeeSetting() {
        this.passwordLastChanged = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getFontSize() { return fontSize; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }

    public Boolean getCompactMode() { return compactMode; }
    public void setCompactMode(Boolean compactMode) { this.compactMode = compactMode; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }

    public String getTimeFormat() { return timeFormat; }
    public void setTimeFormat(String timeFormat) { this.timeFormat = timeFormat; }

    public Boolean getMfaEnabled() { return mfaEnabled; }
    public void setMfaEnabled(Boolean mfaEnabled) { this.mfaEnabled = mfaEnabled; }

    public LocalDateTime getPasswordLastChanged() { return passwordLastChanged; }
    public void setPasswordLastChanged(LocalDateTime passwordLastChanged) { this.passwordLastChanged = passwordLastChanged; }

    public Boolean getProfileVisible() { return profileVisible; }
    public void setProfileVisible(Boolean profileVisible) { this.profileVisible = profileVisible; }

    public Boolean getShowPhoneNumber() { return showPhoneNumber; }
    public void setShowPhoneNumber(Boolean showPhoneNumber) { this.showPhoneNumber = showPhoneNumber; }

    public Boolean getShowEmail() { return showEmail; }
    public void setShowEmail(Boolean showEmail) { this.showEmail = showEmail; }

    public Boolean getShowBirthday() { return showBirthday; }
    public void setShowBirthday(Boolean showBirthday) { this.showBirthday = showBirthday; }

    public Boolean getDailyDigestEnabled() { return dailyDigestEnabled; }
    public void setDailyDigestEnabled(Boolean dailyDigestEnabled) { this.dailyDigestEnabled = dailyDigestEnabled; }

    public String getDigestTime() { return digestTime; }
    public void setDigestTime(String digestTime) { this.digestTime = digestTime; }

    public Boolean getQuietHoursEnabled() { return quietHoursEnabled; }
    public void setQuietHoursEnabled(Boolean quietHoursEnabled) { this.quietHoursEnabled = quietHoursEnabled; }

    public String getQuietHoursFrom() { return quietHoursFrom; }
    public void setQuietHoursFrom(String quietHoursFrom) { this.quietHoursFrom = quietHoursFrom; }

    public String getQuietHoursTo() { return quietHoursTo; }
    public void setQuietHoursTo(String quietHoursTo) { this.quietHoursTo = quietHoursTo; }
}
