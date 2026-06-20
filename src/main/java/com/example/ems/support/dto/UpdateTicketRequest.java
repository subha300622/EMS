package com.example.ems.support.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UpdateTicketRequest {

    @Schema(example = "Request for Leave")
    private String subject;
    @Schema(example = "Detailed description of the item")
    private String description;
    @Schema(example = "string")
    private String priority;
    @Schema(example = "string")
    private String preferredContactMethod;

    public UpdateTicketRequest() {}

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) {
        this.preferredContactMethod = preferredContactMethod;
    }
}
