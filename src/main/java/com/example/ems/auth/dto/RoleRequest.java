package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;



import jakarta.validation.constraints.NotBlank;

public class RoleRequest {

    @NotBlank(message = "Role name is required")
    @Schema(example = "string")
    private String name;

    @Schema(example = "Detailed description of the item")
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
