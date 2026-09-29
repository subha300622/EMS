package com.example.ems.support.dto;

import java.util.List;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

public class MergeTicketsRequest {

    @NotNull(message = "Primary ticket ID is required")
    private Long primaryTicketId;

    @NotEmpty(message = "At least one ticket ID to merge is required")
    private List<Long> mergeTicketIds;

    private String mergeReason;

    public MergeTicketsRequest() {}

    public Long getPrimaryTicketId() { return primaryTicketId; }
    public void setPrimaryTicketId(Long primaryTicketId) { this.primaryTicketId = primaryTicketId; }

    public List<Long> getMergeTicketIds() { return mergeTicketIds; }
    public void setMergeTicketIds(List<Long> mergeTicketIds) { this.mergeTicketIds = mergeTicketIds; }

    public String getMergeReason() { return mergeReason; }
    public void setMergeReason(String mergeReason) { this.mergeReason = mergeReason; }
}
