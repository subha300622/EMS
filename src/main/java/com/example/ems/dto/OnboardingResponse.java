package com.example.ems.dto;

import com.example.ems.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OnboardingResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String employeeEmail;
    private String status;
    private LocalDate startDate;
    private LocalDate completionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OnboardingTaskResponse> tasks;
    private List<OnboardingDocumentResponse> documents;
    private List<AssetResponse> assets;
    private List<TrainingResponse> trainings;

    public OnboardingResponse() {}

    public OnboardingResponse(Onboarding onboarding, 
                              List<OnboardingTask> tasks,
                              List<OnboardingDocument> docs,
                              List<OnboardingAsset> assets,
                              List<OnboardingTraining> trainings) {
        this.id = onboarding.getId();
        if (onboarding.getEmployee() != null) {
            this.employeeId = onboarding.getEmployee().getId();
            this.employeeName = onboarding.getEmployee().getFullName();
            this.employeeEmail = onboarding.getEmployee().getEmail();
        }
        this.status = onboarding.getStatus();
        this.startDate = onboarding.getStartDate();
        this.completionDate = onboarding.getCompletionDate();
        this.createdAt = onboarding.getCreatedAt();
        this.updatedAt = onboarding.getUpdatedAt();

        if (tasks != null) {
            this.tasks = tasks.stream().map(OnboardingTaskResponse::new).collect(Collectors.toList());
        }
        if (docs != null) {
            this.documents = docs.stream().map(OnboardingDocumentResponse::new).collect(Collectors.toList());
        }
        if (assets != null) {
            this.assets = assets.stream().map(AssetResponse::new).collect(Collectors.toList());
        }
        if (trainings != null) {
            this.trainings = trainings.stream().map(TrainingResponse::new).collect(Collectors.toList());
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<OnboardingTaskResponse> getTasks() { return tasks; }
    public void setTasks(List<OnboardingTaskResponse> tasks) { this.tasks = tasks; }

    public List<OnboardingDocumentResponse> getDocuments() { return documents; }
    public void setDocuments(List<OnboardingDocumentResponse> documents) { this.documents = documents; }

    public List<AssetResponse> getAssets() { return assets; }
    public void setAssets(List<AssetResponse> assets) { this.assets = assets; }

    public List<TrainingResponse> getTrainings() { return trainings; }
    public void setTrainings(List<TrainingResponse> trainings) { this.trainings = trainings; }

    // Nested DTO classes
    public static class AssetResponse {
        private Long id;
        private String assetName;
        private String serialNumber;
        private String status;
        private LocalDateTime assignedAt;

        public AssetResponse(OnboardingAsset asset) {
            this.id = asset.getId();
            this.assetName = asset.getAssetName();
            this.serialNumber = asset.getSerialNumber();
            this.status = asset.getStatus();
            this.assignedAt = asset.getAssignedAt();
        }

        public Long getId() { return id; }
        public String getAssetName() { return assetName; }
        public String getSerialNumber() { return serialNumber; }
        public String getStatus() { return status; }
        public LocalDateTime getAssignedAt() { return assignedAt; }
    }

    public static class TrainingResponse {
        private Long id;
        private String courseName;
        private String status;
        private LocalDateTime completedAt;

        public TrainingResponse(OnboardingTraining t) {
            this.id = t.getId();
            this.courseName = t.getCourseName();
            this.status = t.getStatus();
            this.completedAt = t.getCompletedAt();
        }

        public Long getId() { return id; }
        public String getCourseName() { return courseName; }
        public String getStatus() { return status; }
        public LocalDateTime getCompletedAt() { return completedAt; }
    }
}
