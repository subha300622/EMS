package com.example.ems.appraisal.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_timeline_events")
public class AppraisalTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "appraisal_id", nullable = false)
    private Appraisal appraisal;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "action_by_id")
    private Employee actionBy;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Appraisal getAppraisal() { return appraisal; }
    public void setAppraisal(Appraisal appraisal) { this.appraisal = appraisal; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Employee getActionBy() { return actionBy; }
    public void setActionBy(Employee actionBy) { this.actionBy = actionBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
