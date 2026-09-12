package com.example.ems.training.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for specifying options when assigning training to department or team")
public record TrainingAssignmentOptionsRequest(
    @Schema(description = "Whether the training assignment is mandatory", example = "true", defaultValue = "true")
    Boolean mandatory
) {
    public boolean isMandatory() {
        return mandatory == null || Boolean.TRUE.equals(mandatory);
    }
}
