package com.example.ems.offboarding.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Document associated with F&F settlement")
public class FnfDocumentResponse {

    @Schema(description = "Type classification of document: SETTLEMENT_STATEMENT, CLEARANCE_SLIP, EXPERIENCE_LETTER", example = "SETTLEMENT_STATEMENT")
    private String documentType;

    @Schema(description = "Document display title", example = "Full & Final Settlement Statement")
    private String title;

    @Schema(description = "Generated PDF file name", example = "fnf_statement_8001.pdf")
    private String fileName;

    @Schema(description = "Secure download or stream URL", example = "/api/v1/fnf/8001/statement")
    private String downloadUrl;

    @Schema(description = "Document generation status: GENERATED, PENDING, ARCHIVED", example = "GENERATED")
    private String status;

    @Schema(description = "Document creation timestamp", example = "2026-09-18T12:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;

    public FnfDocumentResponse() {}

    public FnfDocumentResponse(String documentType, String title, String fileName, String downloadUrl, String status, LocalDateTime generatedAt) {
        this.documentType = documentType;
        this.title = title;
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.status = status;
        this.generatedAt = generatedAt;
    }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
