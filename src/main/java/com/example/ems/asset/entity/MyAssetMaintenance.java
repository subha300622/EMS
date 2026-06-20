package com.example.ems.asset.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_asset_maintenances")
public class MyAssetMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private MyAsset asset;

    @Column(nullable = false)
    private String issue;

    @Column(nullable = false)
    private String vendor;

    @Column(nullable = false)
    private BigDecimal estimatedCost;

    private BigDecimal actualCost;

    @Column(nullable = false)
    private String status = "UNDER_MAINTENANCE"; // UNDER_MAINTENANCE, COMPLETED

    @Column(nullable = false)
    private LocalDate startDate = LocalDate.now();

    private LocalDate completedDate;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public MyAssetMaintenance() {}

    public MyAssetMaintenance(MyAsset asset, String issue, String vendor, BigDecimal estimatedCost) {
        this.asset = asset;
        this.issue = issue;
        this.vendor = vendor;
        this.estimatedCost = estimatedCost;
        this.status = "UNDER_MAINTENANCE";
        this.startDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyAsset getAsset() {
        return asset;
    }

    public void setAsset(MyAsset asset) {
        this.asset = asset;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public BigDecimal getActualCost() {
        return actualCost;
    }

    public void setActualCost(BigDecimal actualCost) {
        this.actualCost = actualCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
