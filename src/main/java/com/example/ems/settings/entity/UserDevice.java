package com.example.ems.settings.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices", indexes = {
    @Index(name = "idx_user_devices_email", columnList = "user_email")
})
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    private String deviceName;
    private String ipAddress;
    private LocalDateTime lastActive;

    public UserDevice() {}

    public UserDevice(String userEmail, String deviceName, String ipAddress, LocalDateTime lastActive) {
        this.userEmail = userEmail;
        this.deviceName = deviceName;
        this.ipAddress = ipAddress;
        this.lastActive = lastActive;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }
}
