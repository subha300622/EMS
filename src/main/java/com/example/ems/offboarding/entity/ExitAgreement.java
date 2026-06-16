package com.example.ems.offboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exit_agreements")
public class ExitAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offboarding_id", nullable = false)
    private Offboarding offboarding;

    @Column(nullable = false)
    private String agreementType; // e.g. NDA

    @Column(nullable = false)
    private Boolean accepted = false;

    @Column(nullable = false)
    private LocalDateTime signedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "SIGNED";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Offboarding getOffboarding() { return offboarding; }
    public void setOffboarding(Offboarding offboarding) { this.offboarding = offboarding; }

    public String getAgreementType() { return agreementType; }
    public void setAgreementType(String agreementType) { this.agreementType = agreementType; }

    public Boolean getAccepted() { return accepted; }
    public void setAccepted(Boolean accepted) { this.accepted = accepted; }

    public LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(LocalDateTime signedAt) { this.signedAt = signedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
