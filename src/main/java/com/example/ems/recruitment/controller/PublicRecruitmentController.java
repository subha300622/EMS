package com.example.ems.recruitment.controller;

import com.example.ems.common.dto.ApiResponse;
import com.example.ems.recruitment.dto.*;
import com.example.ems.recruitment.service.ApplicationService;
import com.example.ems.recruitment.service.JobService;
import com.example.ems.recruitment.service.OfferService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/recruitment")
public class PublicRecruitmentController {

    @Autowired
    private JobService jobService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private OfferService offerService;

    @GetMapping("/{companySlug}/jobs")
    public ResponseEntity<ApiResponse<List<PublicJobResponse>>> getPublicJobsForCompany(@PathVariable String companySlug) {
        List<PublicJobResponse> jobs = jobService.getPublicJobsForCompany(companySlug);
        return ResponseEntity.ok(ApiResponse.success("Fetched public jobs successfully", jobs));
    }

    @GetMapping("/jobs/{jobSlug}")
    public ResponseEntity<ApiResponse<PublicJobResponse>> getPublicJob(@PathVariable String jobSlug) {
        PublicJobResponse job = jobService.getPublicJob(jobSlug);
        return ResponseEntity.ok(ApiResponse.success("Fetched job details successfully", job));
    }

    public ResponseEntity<ApiResponse<PublicJobResponse>> getPublicJobBySlug(String jobSlug) {
        return getPublicJob(jobSlug);
    }

    @PostMapping("/jobs/{jobId}/applications")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyForJob(
            @PathVariable Long jobId,
            @Valid @RequestBody JobApplicationRequest request) {
        ApplicationResponse response = applicationService.applyForJob(jobId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully", response));
    }

    @PostMapping("/offers/{token}/accept")
    public ResponseEntity<ApiResponse<OfferResponse>> acceptOffer(@PathVariable String token) {
        OfferResponse response = offerService.acceptOfferPublic(token);
        return ResponseEntity.ok(ApiResponse.success("Offer accepted successfully", response));
    }
}
