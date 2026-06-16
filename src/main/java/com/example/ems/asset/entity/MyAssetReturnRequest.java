package com.example.ems.asset.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "my_asset_return_requests")
public class MyAssetReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String returnReference;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private MyAsset asset;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String returnReason;

    @Column(nullable = false)
    private String assetCondition;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "my_asset_return_accessories", joinColumns = @JoinColumn(name = "return_request_id"))
    @Column(name = "accessory")
    private List<String> accessoriesReturned;

    private String comments;

    @Column(nullable = false)
    private String status = "PENDING_IT_VERIFICATION"; // PENDING_IT_VERIFICATION, COMPLETED

    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    public MyAssetReturnRequest() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReturnReference() {
        return returnReference;
    }

    public void setReturnReference(String returnReference) {
        this.returnReference = returnReference;
    }

    public MyAsset getAsset() {
        return asset;
    }

    public void setAsset(MyAsset asset) {
        this.asset = asset;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getReturnReason() {
        return returnReason;
    }

    public void setReturnReason(String returnReason) {
        this.returnReason = returnReason;
    }

    public String getAssetCondition() {
        return assetCondition;
    }

    public void setAssetCondition(String assetCondition) {
        this.assetCondition = assetCondition;
    }

    public List<String> getAccessoriesReturned() {
        return accessoriesReturned;
    }

    public void setAccessoriesReturned(List<String> accessoriesReturned) {
        this.accessoriesReturned = accessoriesReturned;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
