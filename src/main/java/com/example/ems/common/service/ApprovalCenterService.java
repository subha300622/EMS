package com.example.ems.common.service;

import com.example.ems.appraisal.entity.Increment;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.common.dto.ApprovalItemDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.service.LeaveService;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.offboarding.service.OffboardingService;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import com.example.ems.onboarding.service.OnboardingService;
import com.example.ems.performance.entity.Goal;
import com.example.ems.performance.repository.GoalRepository;
import com.example.ems.performance.service.GoalService;
import com.example.ems.performance.dto.GoalDecisionRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApprovalCenterService {

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveService leaveService;

    @Autowired
    private GoalRepository goalRepository;

    @Autowired
    private GoalService goalService;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingService onboardingService;

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private OffboardingService offboardingService;

    @Autowired
    private IncrementRepository incrementRepository;

    @Autowired
    private AppraisalService appraisalService;

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<ApprovalItemDto> getPendingApprovals() {
        List<ApprovalItemDto> items = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 1. Leaves (PENDING)
        List<Leave> pendingLeaves = leaveRepository.findByStatus("PENDING");
        for (Leave leave : pendingLeaves) {
            String requester = leave.getEmployee() != null ? leave.getEmployee().getFullName() : "Unknown";
            String details = String.format("%s Leave: %s to %s. Reason: %s",
                    leave.getLeaveType() != null ? leave.getLeaveType().getName() : "General",
                    leave.getStartDate(), leave.getEndDate(), leave.getReason());
            String dateStr = leave.getAppliedAt() != null ? leave.getAppliedAt().format(formatter) : "";
            items.add(new ApprovalItemDto("LEAVE-" + leave.getId(), "LEAVE", requester, details, dateStr, "PENDING"));
        }

        // 2. Goals (SUBMITTED)
        List<Goal> pendingGoals = goalRepository.findByStatus("SUBMITTED");
        for (Goal goal : pendingGoals) {
            String requester = goal.getEmployee() != null ? goal.getEmployee().getFullName() : "Unknown";
            String details = String.format("Goal: %s. Description: %s", goal.getTitle(), goal.getDescription());
            String dateStr = goal.getCreatedAt() != null ? goal.getCreatedAt().format(formatter) : "";
            items.add(new ApprovalItemDto("GOAL-" + goal.getId(), "GOAL", requester, details, dateStr, "PENDING"));
        }

        // 3. Onboarding (UNDER_REVIEW)
        List<Onboarding> pendingOnboardings = onboardingRepository.findByStatus("UNDER_REVIEW");
        for (Onboarding onboarding : pendingOnboardings) {
            String requester = onboarding.getEmployee() != null ? onboarding.getEmployee().getFullName() : "Unknown";
            String details = String.format("Onboarding profile for employee %s (Start Date: %s)", requester, onboarding.getStartDate());
            String dateStr = onboarding.getCreatedAt() != null ? onboarding.getCreatedAt().format(formatter) : "";
            items.add(new ApprovalItemDto("ONBOARDING-" + onboarding.getId(), "ONBOARDING", requester, details, dateStr, "PENDING"));
        }

        // 4. Offboarding (PENDING)
        List<Offboarding> pendingOffboardings = offboardingRepository.findByStatus("PENDING");
        for (Offboarding offboarding : pendingOffboardings) {
            String requester = offboarding.getEmployee() != null ? offboarding.getEmployee().getFullName() : "Unknown";
            String details = String.format("Exit Offboarding: Last Working Day %s. Reason: %s",
                    offboarding.getExitDate() != null ? offboarding.getExitDate() : offboarding.getRequestedLastWorkingDay(), offboarding.getReason());
            String dateStr = offboarding.getCreatedAt() != null ? offboarding.getCreatedAt().format(formatter) : "";
            items.add(new ApprovalItemDto("OFFBOARDING-" + offboarding.getId(), "OFFBOARDING", requester, details, dateStr, "PENDING"));
        }

        // 5. Salary Revision (PENDING)
        List<Increment> pendingIncrements = incrementRepository.findByStatus("PENDING");
        for (Increment inc : pendingIncrements) {
            String requester = inc.getEmployee() != null ? inc.getEmployee().getFullName() : "Unknown";
            String details = String.format("Salary Revision: Current %s -> New %s (Effective: %s). Reason: %s",
                    inc.getCurrentSalary(), inc.getNewSalary(), inc.getEffectiveDate(), inc.getReason());
            String dateStr = inc.getCreatedAt() != null ? inc.getCreatedAt().format(formatter) : "";
            items.add(new ApprovalItemDto("SALARY_REVISION-" + inc.getId(), "SALARY_REVISION", requester, details, dateStr, "PENDING"));
        }

        return items;
    }

    @Transactional
    public void approveItem(String compoundId, String approverEmail) {
        String[] parts = compoundId.split("-", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid compound ID format. Use {TYPE}-{ID}");
        }
        String type = parts[0].toUpperCase();
        Long id = Long.parseLong(parts[1]);

        Employee approver = employeeRepository.findByEmail(approverEmail).orElse(null);

        switch (type) {
            case "LEAVE":
                leaveService.approveLeave(id, approver);
                break;
            case "GOAL":
                GoalDecisionRequest goalDecision = new GoalDecisionRequest();
                goalDecision.setComments("Approved via unified approval center.");
                goalService.approveGoal(id, goalDecision, approver);
                break;
            case "ONBOARDING":
                onboardingService.approveOnboarding(id);
                break;
            case "OFFBOARDING":
                offboardingService.approveOffboarding(id);
                break;
            case "SALARY_REVISION":
                appraisalService.approveIncrement(id, approverEmail);
                break;
            default:
                throw new IllegalArgumentException("Unknown approval item type: " + type);
        }
    }

    @Transactional
    public void rejectItem(String compoundId, String approverEmail) {
        String[] parts = compoundId.split("-", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid compound ID format. Use {TYPE}-{ID}");
        }
        String type = parts[0].toUpperCase();
        Long id = Long.parseLong(parts[1]);

        Employee approver = employeeRepository.findByEmail(approverEmail).orElse(null);

        switch (type) {
            case "LEAVE":
                leaveService.rejectLeave(id, approver);
                break;
            case "GOAL":
                GoalDecisionRequest goalDecision = new GoalDecisionRequest();
                goalDecision.setComments("Rejected via unified approval center.");
                goalService.rejectGoal(id, goalDecision, approver);
                break;
            case "ONBOARDING":
                Onboarding onboarding = onboardingRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Onboarding not found with ID: " + id));
                onboarding.setStatus("REJECTED");
                onboardingRepository.save(onboarding);
                break;
            case "OFFBOARDING":
                offboardingService.rejectOffboarding(id);
                break;
            case "SALARY_REVISION":
                appraisalService.rejectIncrement(id, approverEmail);
                break;
            default:
                throw new IllegalArgumentException("Unknown approval item type: " + type);
        }
    }
}
