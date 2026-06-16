package com.example.ems.offboarding.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "offboarding_asset_returns")
public class OffboardingAssetReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offboarding_id", nullable = false)
    private Offboarding offboarding;

    @Column(nullable = false)
    private String assetName;

    private String serialNumber;

    @Column(nullable = false)
    private String returnStatus = "PENDING"; // PENDING, RETURNED, DAMAGED

    private LocalDateTime returnedAt;

    private String assetCategory;

    @Column(name = "asset_condition")
    private String condition;

    private String remarks;

    private LocalDate returnDate;

    private String verifiedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Offboarding getOffboarding() { return offboarding; }
    public void setOffboarding(Offboarding offboarding) { this.offboarding = offboarding; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public LocalDateTime getReturnedAt() { return returnedAt; }
    public void setReturnedAt(LocalDateTime returnedAt) { this.returnedAt = returnedAt; }

    public String getAssetCategory() { return assetCategory; }
    public void setAssetCategory(String assetCategory) { this.assetCategory = assetCategory; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
}
