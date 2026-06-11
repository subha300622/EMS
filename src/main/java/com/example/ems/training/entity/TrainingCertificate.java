package com.example.ems.training.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "training_certificates")
public class TrainingCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private TrainingEnrollment enrollment;

    @Column(nullable = false)
    private LocalDate issueDate = LocalDate.now();

    @Column(nullable = false, unique = true)
    private String certificateNumber;

    private String fileUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TrainingEnrollment getEnrollment() { return enrollment; }
    public void setEnrollment(TrainingEnrollment enrollment) { this.enrollment = enrollment; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public String getCertificateNumber() { return certificateNumber; }
    public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
