package com.example.ems.onboarding.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.onboarding.dto.comment.OnboardingCommentCreateRequest;
import com.example.ems.onboarding.dto.comment.OnboardingCommentResponse;
import com.example.ems.onboarding.service.OnboardingCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/onboarding/{onboardingId}/comments")
@CrossOrigin("*")
@Tag(name = "Onboarding Collaboration - Comments")
public class OnboardingCommentController {

    @Autowired
    private OnboardingCommentService commentService;

    @GetMapping
    @Operation(summary = "Get Onboarding Comments List")
    public ResponseEntity<ApiResponse<List<OnboardingCommentResponse>>> getComments(
            @PathVariable Long onboardingId) {
        List<OnboardingCommentResponse> response = commentService.getComments(onboardingId);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", response));
    }

    @PostMapping
    @Operation(summary = "Add Onboarding Comment")
    public ResponseEntity<ApiResponse<OnboardingCommentResponse>> createComment(
            @PathVariable Long onboardingId,
            @Valid @RequestBody OnboardingCommentCreateRequest request) {
        OnboardingCommentResponse response = commentService.createComment(onboardingId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", response));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete Onboarding Comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long onboardingId,
            @PathVariable Long commentId) {
        commentService.deleteComment(onboardingId, commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
