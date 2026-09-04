package com.example.ems.onboarding.dto.task;

import java.time.LocalDate;

public class TaskUpdateRequest {
    private String status;
    private LocalDate dueDate;
    private String remarks;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
