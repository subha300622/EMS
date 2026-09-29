package com.example.ems.appraisal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AppraisalCriterionDto {

    private Long id;

    @NotBlank(message = "Criteria name is required")
    private String name;

    private String description;

    @NotNull(message = "Weight is required")
    private Double weight = 0.0;

    private Boolean required = true;
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
