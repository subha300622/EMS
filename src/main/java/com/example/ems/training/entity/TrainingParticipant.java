package com.example.ems.training.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"training_id", "employee_id"})
})
public class TrainingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_target_type", nullable = false)
    private AssignmentTargetType assignmentTargetType;

    @Column(name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "participation_status", nullable = false)
    private ParticipationStatus participationStatus = ParticipationStatus.PENDING;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "response_note", columnDefinition = "TEXT")
    private String responseNote;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public AssignmentTargetType getAssignmentTargetType() { return assignmentTargetType; }
    public void setAssignmentTargetType(AssignmentTargetType assignmentTargetType) { this.assignmentTargetType = assignmentTargetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public ParticipationStatus getParticipationStatus() { return participationStatus; }
    public void setParticipationStatus(ParticipationStatus participationStatus) { this.participationStatus = participationStatus; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }

    public String getResponseNote() { return responseNote; }
    public void setResponseNote(String responseNote) { this.responseNote = responseNote; }
}
