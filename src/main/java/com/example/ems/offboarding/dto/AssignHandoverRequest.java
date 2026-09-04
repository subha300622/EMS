package com.example.ems.offboarding.dto;

import jakarta.validation.constraints.NotNull;

public class AssignHandoverRequest {
    @NotNull(message = "Handover person ID is required")
    private Long handoverPersonId;

    public Long getHandoverPersonId() { return handoverPersonId; }
    public void setHandoverPersonId(Long handoverPersonId) { this.handoverPersonId = handoverPersonId; }
}
