package com.example.ems.increment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Increment Letter Response")
public class IncrementLetterResponse {

    @Schema(description = "Letter ID", example = "1")
    private Long id;
    @Schema(description = "Recommendation ID", example = "1")
    private Long recommendationId;
    @Schema(description = "Employee ID", example = "1")
    private Long employeeId;
    @Schema(description = "Employee Full Name", example = "John Doe")
    private String employeeName;
    @Schema(description = "Letter reference code", example = "INC/2026/001")
    private String letterReference;
    @Schema(description = "Generated PDF/document download URL", example = "https://example.com/docs/inc-001.pdf")
    private String documentUrl;
    @Schema(description = "Letter text content")
    private String content;
    @Schema(description = "Generation timestamp")
    private LocalDateTime generatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRecommendationId() { return recommendationId; }
    public void setRecommendationId(Long recommendationId) { this.recommendationId = recommendationId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getLetterReference() { return letterReference; }
    public void setLetterReference(String letterReference) { this.letterReference = letterReference; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
}
