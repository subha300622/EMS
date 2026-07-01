package com.example.ems.auth.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_user_email", columnList = "work_email"),
    @Index(name = "idx_user_status", columnList = "status")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String workEmail;

    private String mobileNumber;

    // Optional — provided by user
    private String employeeId;

    private String department;

    private String requestedRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private com.example.ems.organization.entity.Organization organization;

    // Optional
    private String location;

    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private java.time.Instant createdAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.createdAt == null) {
            this.createdAt = java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        }
        if (this.workEmail != null) {
            this.workEmail = this.workEmail.trim().toLowerCase();
        }
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.Instant createdAt) {
        this.createdAt = createdAt;
    }

    public User() {}

    public User(Long id, String userId, String fullName, String workEmail, String mobileNumber, String employeeId, String department, String requestedRole, Role role, String location, String status, String password) {
        this.id = id;
        this.userId = userId;
        this.fullName = fullName;
        this.workEmail = workEmail;
        this.mobileNumber = mobileNumber;
        this.employeeId = employeeId;
        this.department = department;
        this.requestedRole = requestedRole;
        this.role = role;
        this.location = location;
        this.status = status;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getWorkEmail() {
        return workEmail;
    }

    public void setWorkEmail(String workEmail) {
        this.workEmail = workEmail != null ? workEmail.trim().toLowerCase() : null;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(String requestedRole) {
        this.requestedRole = requestedRole;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public com.example.ems.organization.entity.Organization getOrganization() {
        return organization;
    }

    public void setOrganization(com.example.ems.organization.entity.Organization organization) {
        this.organization = organization;
    }
}
