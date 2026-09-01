package com.example.ems.asset.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_action_requests")
public class AssetActionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    private AssetActionType requestType;

    @Column(name = "approval_instance_id", unique = true)
    private Long approvalInstanceId;

    @Column(name = "requested_by", nullable = false)
    private Long requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssetActionStatus status = AssetActionStatus.PENDING_APPROVAL;

    @Lob
    @Column(name = "payload_json")
    private String payloadJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AssetActionRequest() {}

    public AssetActionRequest(Long organizationId, Long assetId, AssetActionType requestType, Long requestedBy, String payloadJson) {
        this.organizationId = organizationId;
        this.assetId = assetId;
        this.requestType = requestType;
        this.requestedBy = requestedBy;
        this.payloadJson = payloadJson;
        this.status = AssetActionStatus.PENDING_APPROVAL;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public AssetActionType getRequestType() {
        return requestType;
    }

    public void setRequestType(AssetActionType requestType) {
        this.requestType = requestType;
    }

    public Long getApprovalInstanceId() {
        return approvalInstanceId;
    }

    public void setApprovalInstanceId(Long approvalInstanceId) {
        this.approvalInstanceId = approvalInstanceId;
    }

    public Long getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(Long requestedBy) {
        this.requestedBy = requestedBy;
    }

    public AssetActionStatus getStatus() {
        return status;
    }

    public void setStatus(AssetActionStatus status) {
        this.status = status;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
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
