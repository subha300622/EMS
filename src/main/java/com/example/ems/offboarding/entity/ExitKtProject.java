package com.example.ems.offboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "exit_kt_projects")
public class ExitKtProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kt_plan_id", nullable = false)
    @JsonIgnore
    private ExitKtPlan ktPlan;

    @Column(nullable = false)
    private String projectName;

    @Column(nullable = false)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, etc.

    @Column(columnDefinition = "TEXT")
    private String handoverNotes;

    @Column(nullable = false)
    private String riskLevel = "MEDIUM"; // LOW, MEDIUM, HIGH

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExitKtPlan getKtPlan() { return ktPlan; }
    public void setKtPlan(ExitKtPlan ktPlan) { this.ktPlan = ktPlan; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHandoverNotes() { return handoverNotes; }
    public void setHandoverNotes(String handoverNotes) { this.handoverNotes = handoverNotes; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
}
