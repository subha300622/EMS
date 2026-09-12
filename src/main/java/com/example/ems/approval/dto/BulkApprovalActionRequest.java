package com.example.ems.approval.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class BulkApprovalActionRequest {

    @NotEmpty(message = "Task IDs must not be empty")
    private List<String> taskIds;

    @Size(max = 255, message = "Comment must not exceed 255 characters")
    private String comment;

    public BulkApprovalActionRequest() {}

    public List<String> getTaskIds() { return taskIds; }
    public void setTaskIds(List<String> taskIds) { this.taskIds = taskIds; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
