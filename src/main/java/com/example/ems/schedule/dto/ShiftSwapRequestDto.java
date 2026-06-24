package com.example.ems.schedule.dto;

public class ShiftSwapRequestDto {
    private Long requestId;
    private ShiftSwapUserDto requester;
    private ShiftSwapUserDto receiver;
    private String requesterShiftType;
    private String receiverShiftType;
    private String requesterShiftDate;
    private String receiverShiftDate;
    private String status; // PENDING, APPROVED, REJECTED

    public ShiftSwapRequestDto() {}

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public ShiftSwapUserDto getRequester() {
        return requester;
    }

    public void setRequester(ShiftSwapUserDto requester) {
        this.requester = requester;
    }

    public ShiftSwapUserDto getReceiver() {
        return receiver;
    }

    public void setReceiver(ShiftSwapUserDto receiver) {
        this.receiver = receiver;
    }

    public String getRequesterShiftType() {
        return requesterShiftType;
    }

    public void setRequesterShiftType(String requesterShiftType) {
        this.requesterShiftType = requesterShiftType;
    }

    public String getReceiverShiftType() {
        return receiverShiftType;
    }

    public void setReceiverShiftType(String receiverShiftType) {
        this.receiverShiftType = receiverShiftType;
    }

    public String getRequesterShiftDate() {
        return requesterShiftDate;
    }

    public void setRequesterShiftDate(String requesterShiftDate) {
        this.requesterShiftDate = requesterShiftDate;
    }

    public String getReceiverShiftDate() {
        return receiverShiftDate;
    }

    public void setReceiverShiftDate(String receiverShiftDate) {
        this.receiverShiftDate = receiverShiftDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
