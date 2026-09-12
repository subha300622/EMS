package com.example.ems.approval.dto;

public class ApprovalActionRequest {

    private String comment;

    public ApprovalActionRequest() {}

    public ApprovalActionRequest(String comment) {
        this.comment = comment;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
