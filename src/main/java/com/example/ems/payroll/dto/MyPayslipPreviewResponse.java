package com.example.ems.payroll.dto;

public class MyPayslipPreviewResponse {

    private Long payslipId;
    private String payslipNumber;
    private String previewUrl;
    private String expiresIn;

    public MyPayslipPreviewResponse() {}

    public MyPayslipPreviewResponse(Long payslipId, String payslipNumber, String previewUrl, String expiresIn) {
        this.payslipId = payslipId;
        this.payslipNumber = payslipNumber;
        this.previewUrl = previewUrl;
        this.expiresIn = expiresIn;
    }

    public Long getPayslipId() { return payslipId; }
    public void setPayslipId(Long payslipId) { this.payslipId = payslipId; }

    public String getPayslipNumber() { return payslipNumber; }
    public void setPayslipNumber(String payslipNumber) { this.payslipNumber = payslipNumber; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public String getExpiresIn() { return expiresIn; }
    public void setExpiresIn(String expiresIn) { this.expiresIn = expiresIn; }
}
