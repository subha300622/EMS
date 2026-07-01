package com.example.ems.employee.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_id", columnList = "employee_id"),
    @Index(name = "idx_employee_email", columnList = "email"),
    @Index(name = "idx_employee_status", columnList = "status"),
    @Index(name = "idx_employee_department", columnList = "department")
})
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String employeeId;

    private String phone;

    private String gender;

    private LocalDate dob;

    private String address;

    private String emergencyContact;

    private String department;

    private String designation;

    private BigDecimal annualSalary;

    private LocalDate joiningDate;

    private String location;

    private String employmentType;

    private String status = "ACTIVE";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties({"manager", "team"})
    private Employee manager;

    private String workMode = "OFFICE";

    private String profileImage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    @JsonIgnoreProperties("manager")
    private MyTeam team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private com.example.ems.organization.entity.Organization organization;

    private String availability = "AVAILABLE";

    private String currentStatus = "WORKING";

    private LocalDateTime lastActiveAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email != null ? email.trim().toLowerCase() : null; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public BigDecimal getAnnualSalary() { return annualSalary; }
    public void setAnnualSalary(BigDecimal annualSalary) { this.annualSalary = annualSalary; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getWorkMode() { return workMode; }
    public void setWorkMode(String workMode) { this.workMode = workMode; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public MyTeam getTeam() { return team; }
    public void setTeam(MyTeam team) { this.team = team; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getCurrentStatus() { return currentStatus; }
    public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public com.example.ems.organization.entity.Organization getOrganization() { return organization; }
    public void setOrganization(com.example.ems.organization.entity.Organization organization) { this.organization = organization; }
}
