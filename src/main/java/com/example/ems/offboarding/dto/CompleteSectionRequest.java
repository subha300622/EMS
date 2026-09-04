package com.example.ems.offboarding.dto;

import jakarta.validation.constraints.NotBlank;

public class CompleteSectionRequest {
    @NotBlank(message = "Section name is required")
    private String section;

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}
