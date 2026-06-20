package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationPreferenceResponse {
    @Schema(example = "john.doe@example.com")
    private boolean emailEnabled;
    @Schema(example = "true")
    private boolean pushEnabled;
    @Schema(example = "true")
    private boolean payrollNotifications;
    @Schema(example = "true")
    private boolean expenseNotifications;
    @Schema(example = "true")
    private boolean systemNotifications;

    public NotificationPreferenceResponse() {}

    public NotificationPreferenceResponse(boolean emailEnabled, boolean pushEnabled, boolean payrollNotifications, boolean expenseNotifications, boolean systemNotifications) {
        this.emailEnabled = emailEnabled;
        this.pushEnabled = pushEnabled;
        this.payrollNotifications = payrollNotifications;
        this.expenseNotifications = expenseNotifications;
        this.systemNotifications = systemNotifications;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }

    public boolean isPayrollNotifications() {
        return payrollNotifications;
    }

    public void setPayrollNotifications(boolean payrollNotifications) {
        this.payrollNotifications = payrollNotifications;
    }

    public boolean isExpenseNotifications() {
        return expenseNotifications;
    }

    public void setExpenseNotifications(boolean expenseNotifications) {
        this.expenseNotifications = expenseNotifications;
    }

    public boolean isSystemNotifications() {
        return systemNotifications;
    }

    public void setSystemNotifications(boolean systemNotifications) {
        this.systemNotifications = systemNotifications;
    }
}
