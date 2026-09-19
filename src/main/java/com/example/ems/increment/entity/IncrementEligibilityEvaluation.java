package com.example.ems.increment.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "increment_eligibility_evaluations", indexes = {
        @Index(name = "idx_increment_eval_cycle_emp", columnList = "cycle_id, employee_id")
})
public class IncrementEligibilityEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id", nullable = false)
    private IncrementCycle cycle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "appraisal_id")
    private Long appraisalId;

    @Column(nullable = false)
    private Boolean eligible;

    @Column(name = "eligibility_status", nullable = false)
    private String eligibilityStatus;

    @Column(name = "ineligible_reason", columnDefinition = "TEXT")
    private String ineligibleReason;

    @Column(name = "eligibility_snapshot", columnDefinition = "TEXT")
    private String eligibilitySnapshot;

    @Column(name = "final_rating")
    private Double finalRating;

    @Column(name = "goal_achievement_percentage")
    private Double goalAchievementPercentage;

    @Column(name = "attendance_percentage")
    private Double attendancePercentage;

    @Column(name = "service_months")
    private Integer serviceMonths;

    @Column(name = "disciplinary_clear")
    private Boolean disciplinaryClear = true;

    @Column(name = "budget_available")
    private Boolean budgetAvailable = true;

    @Column(name = "evaluated_at", updatable = false)
    private LocalDateTime evaluatedAt = LocalDateTime.now();

    public IncrementEligibilityEvaluation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public IncrementCycle getCycle() { return cycle; }
    public void setCycle(IncrementCycle cycle) { this.cycle = cycle; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public Boolean getEligible() { return eligible; }
    public void setEligible(Boolean eligible) { this.eligible = eligible; }

    public String getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(String eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public String getIneligibleReason() { return ineligibleReason; }
    public void setIneligibleReason(String ineligibleReason) { this.ineligibleReason = ineligibleReason; }

    public String getEligibilitySnapshot() { return eligibilitySnapshot; }
    public void setEligibilitySnapshot(String eligibilitySnapshot) { this.eligibilitySnapshot = eligibilitySnapshot; }

    public Double getFinalRating() { return finalRating; }
    public void setFinalRating(Double finalRating) { this.finalRating = finalRating; }

    public Double getGoalAchievementPercentage() { return goalAchievementPercentage; }
    public void setGoalAchievementPercentage(Double goalAchievementPercentage) { this.goalAchievementPercentage = goalAchievementPercentage; }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public Integer getServiceMonths() { return serviceMonths; }
    public void setServiceMonths(Integer serviceMonths) { this.serviceMonths = serviceMonths; }

    public Boolean getDisciplinaryClear() { return disciplinaryClear; }
    public void setDisciplinaryClear(Boolean disciplinaryClear) { this.disciplinaryClear = disciplinaryClear; }

    public Boolean getBudgetAvailable() { return budgetAvailable; }
    public void setBudgetAvailable(Boolean budgetAvailable) { this.budgetAvailable = budgetAvailable; }

    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(LocalDateTime evaluatedAt) { this.evaluatedAt = evaluatedAt; }
}
