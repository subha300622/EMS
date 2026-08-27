package com.example.ems.onboarding.service;

import com.example.ems.auth.entity.User;
import com.example.ems.onboarding.dto.comment.OnboardingCommentCreateRequest;
import com.example.ems.onboarding.dto.comment.OnboardingCommentResponse;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.entity.OnboardingComment;
import com.example.ems.onboarding.repository.OnboardingCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OnboardingCommentService {

    @Autowired
    private OnboardingCommentRepository commentRepository;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    @Autowired
    private OnboardingAuditLogService auditLogService;

    public List<OnboardingCommentResponse> getComments(Long onboardingId) {
        securityValidator.validateAndGetOnboarding(onboardingId);
        return commentRepository.findByOnboardingIdOrderByCreatedAtDesc(onboardingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OnboardingCommentResponse createComment(Long onboardingId, OnboardingCommentCreateRequest request) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        User currentUser = securityValidator.getAuthenticatedUser();

        OnboardingComment comment = new OnboardingComment();
        comment.setOnboarding(onboarding);
        comment.setComment(request.getComment());
        comment.setCreatedBy(currentUser);
        comment.setCreatedAt(LocalDateTime.now());

        OnboardingComment saved = commentRepository.save(comment);
        auditLogService.logAction(onboarding, "COMMENT_ADDED", "ONBOARDING_COMMENT", saved.getId(), "Added onboarding comment");

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteComment(Long onboardingId, Long commentId) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        OnboardingComment comment = securityValidator.validateAndGetComment(onboardingId, commentId);

        commentRepository.delete(comment);
        auditLogService.logAction(onboarding, "COMMENT_DELETED", "ONBOARDING_COMMENT", commentId, "Deleted onboarding comment");
    }

    private OnboardingCommentResponse mapToResponse(OnboardingComment c) {
        OnboardingCommentResponse dto = new OnboardingCommentResponse();
        dto.setCommentId(c.getId());
        dto.setOnboardingId(c.getOnboarding().getId());
        dto.setComment(c.getComment());
        dto.setCreatedAt(c.getCreatedAt());

        String empId = c.getCreatedBy().getEmployeeId() != null ? c.getCreatedBy().getEmployeeId() : "EMP-" + c.getCreatedBy().getId();
        dto.setCreatedBy(new OnboardingCommentResponse.CreatedUser(empId, c.getCreatedBy().getWorkEmail()));
        return dto;
    }
}
