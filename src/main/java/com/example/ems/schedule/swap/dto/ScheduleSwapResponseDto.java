package com.example.ems.schedule.swap.dto;

import com.example.ems.schedule.swap.entity.ScheduleSwapStatus;

public class ScheduleSwapResponseDto {

    private String requestId;
    private ScheduleInfo sourceSchedule;
    private ScheduleInfo targetSchedule;
    private String reason;
    private ScheduleSwapStatus status;
    private WorkflowInfo workflow;

    public ScheduleSwapResponseDto() {}

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public ScheduleInfo getSourceSchedule() { return sourceSchedule; }
    public void setSourceSchedule(ScheduleInfo sourceSchedule) { this.sourceSchedule = sourceSchedule; }

    public ScheduleInfo getTargetSchedule() { return targetSchedule; }
    public void setTargetSchedule(ScheduleInfo targetSchedule) { this.targetSchedule = targetSchedule; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public ScheduleSwapStatus getStatus() { return status; }
    public void setStatus(ScheduleSwapStatus status) { this.status = status; }

    public WorkflowInfo getWorkflow() { return workflow; }
    public void setWorkflow(WorkflowInfo workflow) { this.workflow = workflow; }

    public static class ScheduleInfo {
        private String scheduleId;
        private String employeeId;
        private String employeeName;
        private String date;
        private String startTime;
        private String endTime;
        private String location;

        public ScheduleInfo() {}

        public ScheduleInfo(String scheduleId, String employeeId, String employeeName, String date, String startTime, String endTime, String location) {
            this.scheduleId = scheduleId;
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
        }

        public String getScheduleId() { return scheduleId; }
        public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }

        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class WorkflowInfo {
        private String workflowInstanceId;
        private String workflowType;
        private String currentStep;

        public WorkflowInfo() {}

        public WorkflowInfo(String workflowInstanceId, String workflowType, String currentStep) {
            this.workflowInstanceId = workflowInstanceId;
            this.workflowType = workflowType;
            this.currentStep = currentStep;
        }

        public String getWorkflowInstanceId() { return workflowInstanceId; }
        public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

        public String getWorkflowType() { return workflowType; }
        public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }

        public String getCurrentStep() { return currentStep; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
    }
}
