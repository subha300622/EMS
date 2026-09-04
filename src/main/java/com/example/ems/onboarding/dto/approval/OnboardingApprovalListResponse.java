package com.example.ems.onboarding.dto.approval;

import java.time.LocalDateTime;
import java.util.List;

public class OnboardingApprovalListResponse {

    private Long onboardingId;
    private String status;
    private List<ApprovalItem> approvals;

    public Long getOnboardingId() { return onboardingId; }
    public void setOnboardingId(Long onboardingId) { this.onboardingId = onboardingId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<ApprovalItem> getApprovals() { return approvals; }
    public void setApprovals(List<ApprovalItem> approvals) { this.approvals = approvals; }

    public static class ApprovalItem {
        private Long approvalId;
        private int level;
        private String approverEmployeeId;
        private String approverName;
        private String status;
        private String remarks;
        private LocalDateTime approvedAt;

        public Long getApprovalId() { return approvalId; }
        public void setApprovalId(Long approvalId) { this.approvalId = approvalId; }

        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }

        public String getApproverEmployeeId() { return approverEmployeeId; }
        public void setApproverEmployeeId(String approverEmployeeId) { this.approverEmployeeId = approverEmployeeId; }

        public String getApproverName() { return approverName; }
        public void setApproverName(String approverName) { this.approverName = approverName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        public LocalDateTime getApprovedAt() { return approvedAt; }
        public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    }
}
