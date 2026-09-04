package com.example.ems.expense.dto;

public class ApproveExpenseRequest {
    private String comment;

    public ApproveExpenseRequest() {}

    public ApproveExpenseRequest(String comment) {
        this.comment = comment;
    }

    public String getComment() { return comment; }
    public String getComments() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
