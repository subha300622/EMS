package com.example.ems.offboarding.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "F&F Settlement Workflow Readiness and Blocking Check Details")
public class FnfReadinessDto {

    @Schema(description = "Whether the target action is permitted to proceed", example = "true")
    private boolean ready;

    @Schema(description = "Current F&F settlement status", example = "CALCULATED")
    private String currentStatus;

    @Schema(description = "Target action being evaluated", example = "APPROVAL_SUBMISSION")
    private String targetAction; // APPROVAL_SUBMISSION, PAYMENT_DISBURSEMENT, FINALIZATION

    @Schema(description = "List of unresolved blocker items preventing progression")
    private List<FnfBlockingItemDto> blockingItems;

    @Schema(description = "Summary message of readiness outcome", example = "All departmental clearances verified. Settlement is ready for submission.")
    private String summaryMessage;

    public FnfReadinessDto() {
    }

    public FnfReadinessDto(boolean ready, String currentStatus, String targetAction,
            List<FnfBlockingItemDto> blockingItems, String summaryMessage) {
        this.ready = ready;
        this.currentStatus = currentStatus;
        this.targetAction = targetAction;
        this.blockingItems = blockingItems;
        this.summaryMessage = summaryMessage;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getTargetAction() {
        return targetAction;
    }

    public void setTargetAction(String targetAction) {
        this.targetAction = targetAction;
    }

    public List<FnfBlockingItemDto> getBlockingItems() {
        return blockingItems;
    }

    public void setBlockingItems(List<FnfBlockingItemDto> blockingItems) {
        this.blockingItems = blockingItems;
    }

    public String getSummaryMessage() {
        return summaryMessage;
    }

    public void setSummaryMessage(String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }
}

