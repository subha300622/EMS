package com.example.ems.finance.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "Manager Pending Action item")
public class ManagerPendingActionDto implements Serializable {

    @Schema(description = "Action type identifier", example = "LEAVE_APPROVAL")
    private String type;

    @Schema(description = "Pending items count", example = "4")
    private Long count;

    @Schema(description = "Human-readable action title", example = "Leave Requests Awaiting Approval")
    private String title;

    public ManagerPendingActionDto() {}

    public ManagerPendingActionDto(String type, Long count, String title) {
        this.type = type;
        this.count = count;
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
