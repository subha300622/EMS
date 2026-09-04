package com.example.ems.employee.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_levels")
public class JobLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private com.example.ems.organization.entity.Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", nullable = false)
    private Designation designation;

    @Column(name = "job_level", nullable = false)
    private String jobLevel;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "jobLevel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmploymentType> employmentTypes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        }
        if (this.updatedAt == null) {
            this.updatedAt = this.createdAt;
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public JobLevel() {}

    public JobLevel(Long id, String jobLevel, String description, String status) {
        this.id = id;
        this.jobLevel = jobLevel;
        this.description = description;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public com.example.ems.organization.entity.Organization getOrganization() { return organization; }
    public void setOrganization(com.example.ems.organization.entity.Organization organization) { this.organization = organization; }

    public Designation getDesignation() { return designation; }
    public void setDesignation(Designation designation) { this.designation = designation; }

    public String getJobLevel() { return jobLevel; }
    public void setJobLevel(String jobLevel) { this.jobLevel = jobLevel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<EmploymentType> getEmploymentTypes() { return employmentTypes; }
    public void setEmploymentTypes(List<EmploymentType> employmentTypes) { this.employmentTypes = employmentTypes; }

    public void addEmploymentType(EmploymentType et) {
        employmentTypes.add(et);
        et.setJobLevel(this);
    }

    public void removeEmploymentType(EmploymentType et) {
        employmentTypes.remove(et);
        et.setJobLevel(null);
    }
}
