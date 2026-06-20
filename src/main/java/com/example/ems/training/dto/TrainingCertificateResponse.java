package com.example.ems.training.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.training.entity.TrainingCertificate;

import java.time.LocalDate;

public class TrainingCertificateResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "1")
    private Long enrollmentId;
    @Schema(example = "string")
    private String employeeName;
    @Schema(example = "Project Deliverables")
    private String courseTitle;
    @Schema(example = "string")
    private String trainerName;
    @Schema(example = "2026-06-19")
    private LocalDate scheduleDate;
    @Schema(example = "2026-06-19")
    private LocalDate issueDate;
    @Schema(example = "string")
    private String certificateNumber;
    @Schema(example = "string")
    private String fileUrl;

    public TrainingCertificateResponse() {}

    public TrainingCertificateResponse(TrainingCertificate certificate) {
        this.id = certificate.getId();
        this.issueDate = certificate.getIssueDate();
        this.certificateNumber = certificate.getCertificateNumber();
        this.fileUrl = certificate.getFileUrl();

        if (certificate.getEnrollment() != null) {
            this.enrollmentId = certificate.getEnrollment().getId();

            if (certificate.getEnrollment().getEmployee() != null) {
                this.employeeName = certificate.getEnrollment().getEmployee().getFullName();
            }

            if (certificate.getEnrollment().getSession() != null) {
                this.trainerName = certificate.getEnrollment().getSession().getTrainerName();
                this.scheduleDate = certificate.getEnrollment().getSession().getScheduleDate();

                if (certificate.getEnrollment().getSession().getCourse() != null) {
                    this.courseTitle = certificate.getEnrollment().getSession().getCourse().getTitle();
                }
            }
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Long enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public LocalDate getScheduleDate() { return scheduleDate; }
    public void setScheduleDate(LocalDate scheduleDate) { this.scheduleDate = scheduleDate; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
