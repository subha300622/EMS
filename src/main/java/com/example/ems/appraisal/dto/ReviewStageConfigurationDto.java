package com.example.ems.appraisal.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ReviewStageConfigurationDto {

    private Long id;
    private Integer stageOrder;
    private String stageName;

    @JsonAlias({"permission", "requiredPermission"})
    private String permission = "APPRAISAL_REVIEW";

    private boolean required = true;
    private Double weightage = 1.0;

    public ReviewStageConfigurationDto() {}

    public ReviewStageConfigurationDto(Integer stageOrder, String stageName, String permission, boolean required) {
        this.stageOrder = stageOrder;
        this.stageName = stageName;
        this.permission = permission != null ? permission : "APPRAISAL_REVIEW";
        this.required = required;
        this.weightage = 1.0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getStageOrder() { return stageOrder; }
    public void setStageOrder(Integer stageOrder) { this.stageOrder = stageOrder; }

    public String getStageName() { return stageName; }
    public void setStageName(String stageName) { this.stageName = stageName; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }

    public String getRequiredPermission() { return permission; }
    public void setRequiredPermission(String requiredPermission) { this.permission = requiredPermission; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public Double getWeightage() { return weightage; }
    public void setWeightage(Double weightage) { this.weightage = weightage; }
}
