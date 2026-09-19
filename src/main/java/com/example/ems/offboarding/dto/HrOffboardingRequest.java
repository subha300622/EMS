package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "HR Offboarding Initiation Request")
public class HrOffboardingRequest {

    @NotNull(message = "Last working date is required")
    @Schema(description = "Confirmed last working date", example = "2026-10-17", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate lastWorkingDate;

    @Schema(description = "Notice period in days", example = "30")
    private Integer noticePeriodDays = 30;

    @Schema(description = "Notice days actually served", example = "30")
    private Integer noticeServedDays = 30;

    @Schema(description = "Remarks for offboarding initiation", example = "HR offboarding initiated")
    private String remarks;

    public HrOffboardingRequest() {}

    public LocalDate getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(LocalDate lastWorkingDate) { this.lastWorkingDate = lastWorkingDate; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public Integer getNoticeServedDays() { return noticeServedDays; }
    public void setNoticeServedDays(Integer noticeServedDays) { this.noticeServedDays = noticeServedDays; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
