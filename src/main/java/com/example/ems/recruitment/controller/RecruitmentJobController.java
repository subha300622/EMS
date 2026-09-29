package com.example.ems.recruitment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.recruitment.dto.JobCreateRequest;
import com.example.ems.recruitment.dto.JobResponse;
import com.example.ems.recruitment.dto.JobUpdateRequest;
import com.example.ems.recruitment.service.JobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recruitment/jobs")
public class RecruitmentJobController {

    @Autowired
    private JobService jobService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(@Valid @RequestBody JobCreateRequest request) {
        JobResponse response = jobService.createJob(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job created successfully", response));
    }

    @PutMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long jobId,
            @RequestBody JobUpdateRequest request) {
        JobResponse response = jobService.updateJob(jobId, request);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", response));
    }

    @PatchMapping("/{jobId}/publish")
    public ResponseEntity<ApiResponse<JobResponse>> publishJob(@PathVariable Long jobId) {
        JobResponse response = jobService.publishJob(jobId);
        return ResponseEntity.ok(ApiResponse.success("Job published successfully", response));
    }

    @PatchMapping("/{jobId}/close")
    public ResponseEntity<ApiResponse<JobResponse>> closeJob(@PathVariable Long jobId) {
        JobResponse response = jobService.closeJob(jobId);
        return ResponseEntity.ok(ApiResponse.success("Job closed successfully", response));
    }

    @PatchMapping("/{jobId}/reopen")
    public ResponseEntity<ApiResponse<JobResponse>> reopenJob(@PathVariable Long jobId) {
        JobResponse response = jobService.reopenJob(jobId);
        return ResponseEntity.ok(ApiResponse.success("Job reopened successfully", response));
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJob(jobId);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    @PostMapping("/{jobId}/duplicate")
    public ResponseEntity<ApiResponse<JobResponse>> duplicateJob(@PathVariable Long jobId) {
        JobResponse response = jobService.duplicateJob(jobId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job duplicated successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> getHRJobs() {
        List<JobResponse> jobs = jobService.getHRJobs();
        return ResponseEntity.ok(ApiResponse.success("Fetched jobs successfully", jobs));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiResponse<JobResponse>> getHRJobById(@PathVariable Long jobId) {
        JobResponse job = jobService.getHRJobById(jobId);
        return ResponseEntity.ok(ApiResponse.success("Fetched job details successfully", job));
    }
}
