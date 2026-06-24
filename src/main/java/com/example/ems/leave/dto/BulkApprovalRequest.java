package com.example.ems.leave.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class BulkApprovalRequest {
    @NotEmpty(message = "Leave IDs must not be empty")
    private List<Long> leaveIds;

    @Size(max = 255, message = "Comment must not exceed 255 characters")
    private String comment;

    public BulkApprovalRequest() {}

    public List<Long> getLeaveIds() { return leaveIds; }
    public void setLeaveIds(List<Long> leaveIds) { this.leaveIds = leaveIds; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
