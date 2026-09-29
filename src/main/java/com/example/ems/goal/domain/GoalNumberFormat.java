package com.example.ems.goal.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "goal_number_formats", uniqueConstraints = {
    @UniqueConstraint(name = "uk_goal_num_format_org", columnNames = {"organization_id"})
})
public class GoalNumberFormat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(length = 20)
    private String prefix = "GOAL";

    @Column(name = "sequence_length")
    private Integer sequenceLength = 5;

    @Column(name = "current_sequence")
    private Long currentSequence = 1L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public GoalNumberFormat() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public Integer getSequenceLength() { return sequenceLength; }
    public void setSequenceLength(Integer sequenceLength) { this.sequenceLength = sequenceLength; }

    public Long getCurrentSequence() { return currentSequence; }
    public void setCurrentSequence(Long currentSequence) { this.currentSequence = currentSequence; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
