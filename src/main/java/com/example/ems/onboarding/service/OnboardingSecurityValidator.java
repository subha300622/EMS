package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.onboarding.entity.*;
import com.example.ems.onboarding.repository.*;
import com.example.ems.security.context.SecurityContextFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OnboardingSecurityValidator {

    @Autowired
    private SecurityContextFacade securityContextFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingTaskRepository taskRepository;

    @Autowired
    private OnboardingPhaseRepository phaseRepository;

    @Autowired
    private OnboardingCommentRepository commentRepository;

    @Autowired
    private OnboardingApprovalRepository approvalRepository;

    public User getAuthenticatedUser() {
        String email = securityContextFacade.getEmail();
        if (email == null) {
            throw new AccessDeniedException("Unauthorized - missing authentication context");
        }
        return userRepository.findByWorkEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Unauthorized - authenticated user not found"));
    }

    public Onboarding validateAndGetOnboarding(Long onboardingId) {
        User user = getAuthenticatedUser();
        Onboarding onboarding = onboardingRepository.findById(onboardingId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding not found with ID: " + onboardingId));

        validateOrganizationOwnership(user, onboarding);
        return onboarding;
    }

    public OnboardingTask validateAndGetTask(Long onboardingId, Long taskId) {
        Onboarding onboarding = validateAndGetOnboarding(onboardingId);
        OnboardingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (!task.getOnboarding().getId().equals(onboarding.getId())) {
            throw new ResourceNotFoundException("Task ID " + taskId + " does not belong to Onboarding ID " + onboardingId);
        }
        return task;
    }

    public OnboardingPhase validateAndGetPhase(Long onboardingId, Long phaseId) {
        Onboarding onboarding = validateAndGetOnboarding(onboardingId);
        OnboardingPhase phase = phaseRepository.findById(phaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Phase not found with ID: " + phaseId));

        if (!phase.getOnboarding().getId().equals(onboarding.getId())) {
            throw new ResourceNotFoundException("Phase ID " + phaseId + " does not belong to Onboarding ID " + onboardingId);
        }
        return phase;
    }

    public OnboardingComment validateAndGetComment(Long onboardingId, Long commentId) {
        Onboarding onboarding = validateAndGetOnboarding(onboardingId);
        OnboardingComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        if (!comment.getOnboarding().getId().equals(onboarding.getId())) {
            throw new ResourceNotFoundException("Comment ID " + commentId + " does not belong to Onboarding ID " + onboardingId);
        }
        return comment;
    }

    public OnboardingApproval validateAndGetApproval(Long onboardingId, Long approvalId) {
        Onboarding onboarding = validateAndGetOnboarding(onboardingId);
        OnboardingApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException("Approval not found with ID: " + approvalId));

        if (!approval.getOnboarding().getId().equals(onboarding.getId())) {
            throw new ResourceNotFoundException("Approval ID " + approvalId + " does not belong to Onboarding ID " + onboardingId);
        }
        return approval;
    }

    public void validateOrganizationOwnership(User user, Onboarding onboarding) {
        // Platform Admin is the ONLY role that operates platform-wide without a specific organizationId
        String roleName = user.getRole() != null ? user.getRole().getName() : "";
        if ("PLATFORM_ADMIN".equalsIgnoreCase(roleName)) {
            return;
        }

        Employee candidate = onboarding.getEmployee();

        // Self-access check based on employee ID or work email
        boolean isSelf = (user.getUserId() != null && candidate.getEmployeeId() != null && user.getUserId().equals(candidate.getEmployeeId()))
                || (user.getWorkEmail() != null && candidate.getEmail() != null && user.getWorkEmail().equalsIgnoreCase(candidate.getEmail()));

        if (isSelf) {
            return;
        }

        // Check organization context via TenantContext thread-local
        Long activeOrgId = com.example.ems.security.context.TenantContext.getOrganizationId();
        if (activeOrgId == null && user.getOrganization() != null) {
            activeOrgId = user.getOrganization().getId();
        }

        Long candidateOrgId = candidate.getOrganization() != null ? candidate.getOrganization().getId() : null;

        if (activeOrgId != null && candidateOrgId != null && !activeOrgId.equals(candidateOrgId)) {
            throw new AccessDeniedException("Access denied - cross-organization onboarding access is strictly forbidden");
        }
    }
}
