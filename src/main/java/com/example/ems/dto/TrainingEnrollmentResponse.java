package com.example.ems.dto;

import com.example.ems.entity.TrainingEnrollment;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TrainingEnrollmentResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private Long sessionId;
    private String courseTitle;
    private String trainerName;
    private LocalDate scheduleDate;
    private LocalDateTime enrollmentDate;
    private String status;
    private Integer progressPercent;
    private String grade;
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
