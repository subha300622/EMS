package com.example.ems.asset.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_asset_issues")
public class MyAssetIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private MyAsset asset;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String issueType; // HARDWARE, SOFTWARE, NETWORK

    @Column(nullable = false)
    private String severity = "MEDIUM"; // LOW, MEDIUM, HIGH

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED, CLOSED

    @Column(nullable = false)
    private LocalDateTime reportedAt = LocalDateTime.now();

    private String assignedTeam = "IT Support";

    private LocalDate resolutionETA;

    public MyAssetIssue() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public MyAsset getAsset() {
        return asset;
    }

    public void setAsset(MyAsset asset) {
        this.asset = asset;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(LocalDateTime reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getAssignedTeam() {
        return assignedTeam;
    }

    public void setAssignedTeam(String assignedTeam) {
        this.assignedTeam = assignedTeam;
    }

    public LocalDate getResolutionETA() {
        return resolutionETA;
    }

    public void setResolutionETA(LocalDate resolutionETA) {
        this.resolutionETA = resolutionETA;
    }
}
