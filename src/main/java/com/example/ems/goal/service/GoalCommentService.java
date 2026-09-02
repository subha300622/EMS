package com.example.ems.goal.service;

import com.example.ems.goal.domain.GoalComment;
import com.example.ems.goal.dto.GoalCommentRequest;
import com.example.ems.goal.repository.GoalCommentRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoalCommentService {

    @Autowired
    @Qualifier("enterpriseGoalCommentRepository")
    private GoalCommentRepository commentRepository;

    @Autowired
    private GoalActivityService activityService;

    @Transactional
    public GoalComment addComment(Long goalId, GoalCommentRequest request, Long createdById, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalComment comment = new GoalComment();
        comment.setOrganizationId(orgId);
        comment.setGoalId(goalId);
        comment.setParentCommentId(request.getParentCommentId());
        comment.setComment(request.getComment());
        comment.setCreatedBy(createdById);
        comment.setIsEdited(false);
        comment.setIsDeleted(false);

        GoalComment saved = commentRepository.save(comment);

        activityService.logActivity(
                goalId,
                createdById,
                actorName,
                actorRole,
                "COMMENT_ADDED",
                "Added a comment",
                "{\"commentId\":" + saved.getId() + "}"
        );

        return saved;
    }

    @Transactional
    public void deleteComment(Long commentId, Long actorId, String actorName, String actorRole) {
        Long orgId = TenantContext.requireOrganizationId();
        GoalComment comment = commentRepository.findByIdAndOrganizationIdAndIsDeletedFalse(commentId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

        comment.setIsDeleted(true);
        commentRepository.save(comment);
    }

    public List<GoalComment> getComments(Long goalId) {
        Long orgId = TenantContext.requireOrganizationId();
        return commentRepository.findByOrganizationIdAndGoalIdAndIsDeletedFalseOrderByCreatedAtAsc(orgId, goalId);
    }
}
