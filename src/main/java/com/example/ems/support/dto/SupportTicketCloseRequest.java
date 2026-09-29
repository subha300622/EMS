package com.example.ems.support.dto;

public class SupportTicketCloseRequest {

    private String comment;

    public SupportTicketCloseRequest() {}

    public SupportTicketCloseRequest(String comment) {
        this.comment = comment;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
