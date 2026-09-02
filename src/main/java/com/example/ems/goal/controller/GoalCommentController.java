package com.example.ems.goal.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.goal.domain.GoalComment;
import com.example.ems.goal.dto.GoalCommentRequest;
import com.example.ems.goal.service.GoalCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals/{goalId}/comments")
@CrossOrigin("*")
@Tag(name = "Goal Collaboration & Comments", description = "APIs for Goal Discussions and Replies")
public class GoalCommentController {

    @Autowired
    private GoalCommentService commentService;

    @Operation(summary = "Add Comment", description = "Adds a comment or reply to a goal")
    @PostMapping
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> addComment(
            @PathVariable("goalId") Long goalId,
            @Valid @RequestBody GoalCommentRequest request) {
        GoalComment comment = commentService.addComment(goalId, request, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Comment added successfully", comment));
    }

    @Operation(summary = "Get Goal Comments", description = "Lists comments for a goal")
    @GetMapping
    @PreAuthorize("hasAuthority('GOAL_VIEW') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> getComments(@PathVariable("goalId") Long goalId) {
        List<GoalComment> comments = commentService.getComments(goalId);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", comments));
    }

    @Operation(summary = "Delete Comment", description = "Soft deletes a comment")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAuthority('GOAL_EDIT') or hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Object>> deleteComment(
            @PathVariable("goalId") Long goalId,
            @PathVariable("commentId") Long commentId) {
        commentService.deleteComment(commentId, 1L, "User", "EMPLOYEE");
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
