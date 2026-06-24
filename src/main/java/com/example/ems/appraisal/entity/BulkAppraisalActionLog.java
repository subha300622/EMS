package com.example.ems.appraisal.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bulk_appraisal_action_logs")
public class BulkAppraisalActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operationType; // APPROVE or REJECT

    @ManyToOne(optional = false)
    @JoinColumn(name = "executed_by_id", nullable = false)
    private Employee executedBy;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @OneToMany(mappedBy = "actionLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BulkAppraisalItemResult> results = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public Employee getExecutedBy() { return executedBy; }
    public void setExecutedBy(Employee executedBy) { this.executedBy = executedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public List<BulkAppraisalItemResult> getResults() { return results; }
    public void setResults(List<BulkAppraisalItemResult> results) { this.results = results; }
}
