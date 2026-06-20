package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class UnreadCountResponse {
    @Schema(example = "1")
    private int unreadCount;

    public UnreadCountResponse() {}

    public UnreadCountResponse(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
