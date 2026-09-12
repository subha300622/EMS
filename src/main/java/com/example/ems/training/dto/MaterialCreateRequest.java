package com.example.ems.training.dto;

import com.example.ems.training.entity.MaterialType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MaterialCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Material type is required")
    private MaterialType materialType;

    @NotBlank(message = "URL or file path is required")
    private String urlOrFilePath;

    private Long fileSizeBytes;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public MaterialType getMaterialType() { return materialType; }
    public void setMaterialType(MaterialType materialType) { this.materialType = materialType; }

    public String getUrlOrFilePath() { return urlOrFilePath; }
    public void setUrlOrFilePath(String urlOrFilePath) { this.urlOrFilePath = urlOrFilePath; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
}
