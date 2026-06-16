package com.example.ems.employee.dto;

public class MyDocumentPreviewResponse {

    private Long documentId;
    private String documentName;
    private String previewUrl;
    private int expiresInSeconds;

    public MyDocumentPreviewResponse() {}

    public MyDocumentPreviewResponse(Long documentId, String documentName, String previewUrl, int expiresInSeconds) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.previewUrl = previewUrl;
        this.expiresInSeconds = expiresInSeconds;
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
    public int getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(int expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
}
