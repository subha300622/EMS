package com.example.ems.recruitment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.recruitment.dto.*;
import com.example.ems.recruitment.entity.ApplicationStatus;
import com.example.ems.recruitment.service.ApplicationService;
import com.example.ems.recruitment.service.EmployeeConversionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruitment/applications")
public class RecruitmentApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private EmployeeConversionService employeeConversionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getHRApplications(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double experienceMin,
            @RequestParam(required = false) Double experienceMax,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Page<ApplicationResponse> applications = applicationService.getHRApplications(
                jobId, status, departmentId, location, experienceMin, experienceMax, search, PageRequest.of(page, size, sort));

        return ResponseEntity.ok(ApiResponse.success("Fetched applications successfully", applications));
    }

    @GetMapping("/{applicationId}")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(@PathVariable Long applicationId) {
        ApplicationResponse app = applicationService.getApplicationById(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Fetched application details successfully", app));
    }

    @GetMapping("/{applicationId}/history")
    public ResponseEntity<ApiResponse<List<ApplicationStatusHistoryResponse>>> getStatusHistory(@PathVariable Long applicationId) {
        List<ApplicationStatusHistoryResponse> history = applicationService.getStatusHistory(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Fetched application status history successfully", history));
    }

    @PostMapping("/{applicationId}/shortlist")
    public ResponseEntity<ApiResponse<ApplicationResponse>> shortlistCandidate(@PathVariable Long applicationId) {
        ApplicationResponse response = applicationService.shortlistCandidate(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Candidate shortlisted successfully", response));
    }

    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<ApiResponse<ApplicationResponse>> rejectCandidate(
            @PathVariable Long applicationId,
            @Valid @RequestBody CandidateRejectRequest request) {
        ApplicationResponse response = applicationService.rejectCandidate(applicationId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success("Candidate rejected successfully", response));
    }

    @PostMapping("/{applicationId}/select")
    public ResponseEntity<ApiResponse<ApplicationResponse>> selectCandidate(@PathVariable Long applicationId) {
        ApplicationResponse response = applicationService.selectCandidate(applicationId);
        return ResponseEntity.ok(ApiResponse.success("Candidate selected successfully", response));
    }

    @PostMapping("/{applicationId}/convert-to-employee")
    public ResponseEntity<ApiResponse<ApplicationResponse>> convertToEmployee(
            @PathVariable Long applicationId,
            @Valid @RequestBody CandidateConversionRequest request) {
        ApplicationResponse response = employeeConversionService.convertToEmployee(applicationId, request);
        return ResponseEntity.ok(ApiResponse.success("Candidate converted to employee successfully", response));
    }
}
