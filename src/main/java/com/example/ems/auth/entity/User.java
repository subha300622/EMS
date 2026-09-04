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

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String department;

    private String requestedRole;

    @Column(name = "role_id")
    private Long roleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @Column(name = "organization_id")
    private Long organizationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private com.example.ems.organization.entity.Organization organization;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "designation_id")
    private Long designationId;

    @Column(name = "job_level_id")
    private Long jobLevelId;

    @Column(name = "employment_type_id")
    private Long employmentTypeId;

    @Column(name = "reporting_manager_id")
    private Long reportingManagerId;

    // Optional
    private String location;

    @Column(name = "organization_name")
    private String organizationName;

    private String branch;

    @Transient
    private UserStatus statusEnum = UserStatus.ACTIVE;

    private String status = "ACTIVE";

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

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
        this.provider = AuthProvider.LOCAL;
        try {
            this.statusEnum = UserStatus.valueOf(status);
        } catch (Exception e) {
            this.statusEnum = UserStatus.ACTIVE;
        }
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
        this.roleId = (role != null) ? role.getId() : null;
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
        try {
            this.statusEnum = UserStatus.valueOf(status);
        } catch (Exception e) {
        }
    }

    public UserStatus getStatusEnum() {
        return statusEnum;
    }

    public void setStatusEnum(UserStatus statusEnum) {
        this.statusEnum = statusEnum;
        if (statusEnum != null) {
            this.status = statusEnum.name();
        }
    }


    public AuthProvider getProvider() {
        return provider;
    }

    public void setProvider(AuthProvider provider) {
        this.provider = provider;
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
        this.organizationId = (organization != null) ? organization.getId() : null;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    // --- New Multi-Tenant Property Accessors ---

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public Long getDesignationId() { return designationId; }
    public void setDesignationId(Long designationId) { this.designationId = designationId; }

    public Long getJobLevelId() { return jobLevelId; }
    public void setJobLevelId(Long jobLevelId) { this.jobLevelId = jobLevelId; }

    public Long getEmploymentTypeId() { return employmentTypeId; }
    public void setEmploymentTypeId(Long employmentTypeId) { this.employmentTypeId = employmentTypeId; }

    public Long getReportingManagerId() { return reportingManagerId; }
    public void setReportingManagerId(Long reportingManagerId) { this.reportingManagerId = reportingManagerId; }
}
