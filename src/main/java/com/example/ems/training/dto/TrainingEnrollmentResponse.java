package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.training.entity.TrainingEnrollment;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrainingEnrollmentResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long employeeId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "john.doe@example.com")
    private String employeeEmail;
    @Schema(example = "1")
    private Long sessionId;
    @Schema(example = "Project Deliverables")
    private String courseTitle;
    @Schema(example = "string")
    private String trainerName;
    @Schema(example = "2026-06-19")
    private LocalDate scheduleDate;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime enrollmentDate;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "75")
    private Integer progressPercent;
    @Schema(example = "string")
    private String grade;
    @Schema(example = "string")
    private String certificateNumber;

    public TrainingEnrollmentResponse() {}

    public TrainingEnrollmentResponse(TrainingEnrollment enrollment) {
        this.id = enrollment.getId();
        this.enrollmentDate = enrollment.getEnrollmentDate();
        this.status = enrollment.getStatus();
        this.progressPercent = enrollment.getProgressPercent();
        this.grade = enrollment.getGrade();

        if (enrollment.getEmployee() != null) {
            this.employeeId = enrollment.getEmployee().getId();
            this.employeeName = enrollment.getEmployee().getFullName();
            this.employeeEmail = enrollment.getEmployee().getEmail();
        }

        if (enrollment.getSession() != null) {
            this.sessionId = enrollment.getSession().getId();
            this.trainerName = enrollment.getSession().getTrainerName();
            this.scheduleDate = enrollment.getSession().getScheduleDate();

            if (enrollment.getSession().getCourse() != null) {
                this.courseTitle = enrollment.getSession().getCourse().getTitle();
            }
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeeEmail() { return employeeEmail; }
    public void setEmployeeEmail(String employeeEmail) { this.employeeEmail = employeeEmail; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public LocalDateTime getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDateTime enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
}
