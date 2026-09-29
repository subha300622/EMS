package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Response returned after HR initiates offboarding")
public class OffboardingInitiationResponse {

    private Long exitId;
    private Long employeeId;
    private String status;
    private LocalDate lastWorkingDate;
    private Integer noticePeriodDays;
    private Integer noticeServedDays;
    private List<ExitClearanceDto> clearances;

    public OffboardingInitiationResponse() {}

    public OffboardingInitiationResponse(Long exitId, Long employeeId, String status, LocalDate lastWorkingDate, Integer noticePeriodDays, Integer noticeServedDays, List<ExitClearanceDto> clearances) {
        this.exitId = exitId;
        this.employeeId = employeeId;
        this.status = status;
        this.lastWorkingDate = lastWorkingDate;
        this.noticePeriodDays = noticePeriodDays;
        this.noticeServedDays = noticeServedDays;
        this.clearances = clearances;
    }

    public Long getExitId() { return exitId; }
    public void setExitId(Long exitId) { this.exitId = exitId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getLastWorkingDate() { return lastWorkingDate; }
    public void setLastWorkingDate(LocalDate lastWorkingDate) { this.lastWorkingDate = lastWorkingDate; }

    public Integer getNoticePeriodDays() { return noticePeriodDays; }
    public void setNoticePeriodDays(Integer noticePeriodDays) { this.noticePeriodDays = noticePeriodDays; }

    public Integer getNoticeServedDays() { return noticeServedDays; }
    public void setNoticeServedDays(Integer noticeServedDays) { this.noticeServedDays = noticeServedDays; }

    public List<ExitClearanceDto> getClearances() { return clearances; }
    public void setClearances(List<ExitClearanceDto> clearances) { this.clearances = clearances; }
}
