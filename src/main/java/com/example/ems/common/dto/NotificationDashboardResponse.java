package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class NotificationDashboardResponse {
    @Schema(example = "1")
    private long unreadToday;
    @Schema(example = "1")
    private long pendingActions;
    @Schema(example = "1")
    private long resolvedToday;
    @Schema(example = "1")
    private long totalNotifications;

    public NotificationDashboardResponse() {}

    public NotificationDashboardResponse(long unreadToday, long pendingActions, long resolvedToday, long totalNotifications) {
        this.unreadToday = unreadToday;
        this.pendingActions = pendingActions;
        this.resolvedToday = resolvedToday;
        this.totalNotifications = totalNotifications;
    }

    public long getUnreadToday() {
        return unreadToday;
    }

    public void setUnreadToday(long unreadToday) {
        this.unreadToday = unreadToday;
    }

    public long getPendingActions() {
        return pendingActions;
    }

    public void setPendingActions(long pendingActions) {
        this.pendingActions = pendingActions;
    }

    public long getResolvedToday() {
        return resolvedToday;
    }

    public void setResolvedToday(long resolvedToday) {
        this.resolvedToday = resolvedToday;
    }

    public long getTotalNotifications() {
        return totalNotifications;
    }

    public void setTotalNotifications(long totalNotifications) {
        this.totalNotifications = totalNotifications;
    }
}
