package com.example.ems.finance.dto;

public class ApproveRequest {
    private String remarks;

    public ApproveRequest() {}

    public ApproveRequest(String remarks) {
        this.remarks = remarks;
    }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
