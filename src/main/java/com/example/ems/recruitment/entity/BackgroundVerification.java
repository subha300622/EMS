package com.example.ems.recruitment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "background_verifications")
public class BackgroundVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    private String verificationAgency;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, PASSED, FAILED

    @Column(columnDefinition = "TEXT")
    private String reportsMetadata;

    public BackgroundVerification() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getVerificationAgency() {
        return verificationAgency;
    }

    public void setVerificationAgency(String verificationAgency) {
        this.verificationAgency = verificationAgency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReportsMetadata() {
        return reportsMetadata;
    }

    public void setReportsMetadata(String reportsMetadata) {
        this.reportsMetadata = reportsMetadata;
    }
}
