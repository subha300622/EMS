package com.example.ems.recruitment.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.recruitment.dto.CandidateRequest;
import com.example.ems.recruitment.dto.CandidateResponse;
import com.example.ems.recruitment.dto.InterviewFeedbackRequest;
import com.example.ems.recruitment.dto.InterviewRequest;
import com.example.ems.recruitment.dto.InterviewResponse;
import com.example.ems.recruitment.dto.JobRequest;
import com.example.ems.recruitment.dto.JobResponse;
import com.example.ems.recruitment.dto.OfferRequest;
import com.example.ems.recruitment.dto.OfferResponse;
import com.example.ems.recruitment.dto.RecruitmentDashboardResponse;
import com.example.ems.recruitment.dto.BackgroundVerificationRequest;
import com.example.ems.recruitment.entity.Candidate;
import com.example.ems.recruitment.entity.BackgroundVerification;



import com.example.ems.recruitment.service.RecruitmentService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Recruitment Management")
public class RecruitmentController {

    @Autowired
    private RecruitmentService recruitmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    private boolean checkRecruitmentPermission(User user) {
        return roleService.hasPermission(user.getWorkEmail(), "recruitment.manage");
    }

    // ── 1. GET RECRUITMENT DASHBOARD ──────────────────────────────────────────
    @GetMapping("/recruitments/dashboard")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        RecruitmentDashboardResponse stats = recruitmentService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Recruitment dashboard metrics retrieved successfully", stats));
    }

    // ── 2. JOBS ENDPOINTS ──────────────────────────────────────────────────
    @PostMapping("/recruitments/jobs")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createJob(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody JobRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        JobResponse response = recruitmentService.createJob(request);
        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posting created successfully", response));
    }

    @GetMapping("/recruitments/jobs")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listJobs(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        // Let all employees view active/all jobs for reference, or restrict to recruitment.manage.
        // Usually, anyone can view jobs. Let's make it open to authenticated users.
        List<JobResponse> jobs = recruitmentService.getJobs();
        return ResponseEntity.ok(ApiResponse.success("Jobs retrieved successfully", jobs));
    }

    @GetMapping("/recruitments/jobs/{jobId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getJob(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long jobId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        JobResponse response = recruitmentService.getJobById(jobId).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Job not found with ID: " + jobId, "JOB_001"));
        }
        return ResponseEntity.ok(ApiResponse.success("Job details retrieved successfully", response));
    }

    @GetMapping("/recruitments/jobs/{jobId}/candidates")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getJobCandidates(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long jobId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        JobResponse response = recruitmentService.getJobById(jobId).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Job not found with ID: " + jobId, "JOB_001"));
        }

        List<CandidateResponse> candidates = recruitmentService.getCandidatesByJobId(jobId);
        return ResponseEntity.ok(ApiResponse.success("Candidates for job retrieved successfully", candidates));
    }

    @PatchMapping("/recruitments/jobs/{jobId}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateJobStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long jobId,
            @RequestBody Map<String, String> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Status field is required", "VAL_001"));
        }

        JobResponse updated = recruitmentService.updateJobStatus(jobId, status).orElse(null);
        if (updated == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Job not found with ID: " + jobId, "JOB_001"));
        }
        return ResponseEntity.ok(ApiResponse.success("Job status updated to " + status.toUpperCase(), updated));
    }

    // ── 3. CANDIDATES ENDPOINTS ─────────────────────────────────────────────
    @PostMapping("/recruitments/candidates")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createCandidate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CandidateRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        try {
            CandidateResponse response = recruitmentService.createCandidate(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Candidate profile registered successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAND_001"));
        }
    }

    @GetMapping("/recruitments/candidates")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listCandidates(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        List<CandidateResponse> candidates = recruitmentService.getCandidates();
        return ResponseEntity.ok(ApiResponse.success("Candidates retrieved successfully", candidates));
    }

    @GetMapping("/recruitments/candidates/{candidateId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getCandidate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long candidateId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        CandidateResponse response = recruitmentService.getCandidateById(candidateId).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Candidate not found with ID: " + candidateId, "CAND_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Candidate details retrieved successfully", response));
    }

    @PatchMapping("/recruitments/candidates/{candidateId}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateCandidateStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long candidateId,
            @RequestBody Map<String, String> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Status field is required", "VAL_001"));
        }

        CandidateResponse updated = recruitmentService.updateCandidateStatus(candidateId, status).orElse(null);
        if (updated == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Candidate not found with ID: " + candidateId, "CAND_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Candidate status updated to " + status.toUpperCase(), updated));
    }

    // ── 4. INTERVIEWS ENDPOINTS ─────────────────────────────────────────────
    @PostMapping("/recruitments/interviews")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> scheduleInterview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody InterviewRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        try {
            InterviewResponse response = recruitmentService.scheduleInterview(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Interview scheduled successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "INT_001"));
        }
    }

    @GetMapping("/recruitments/interviews")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> listInterviews(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        List<InterviewResponse> interviews = recruitmentService.getInterviews();
        return ResponseEntity.ok(ApiResponse.success("Scheduled interviews retrieved successfully", interviews));
    }

    @PostMapping("/recruitments/interviews/{interviewId}/feedback")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> addFeedback(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewFeedbackRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        InterviewResponse response = recruitmentService.addInterviewFeedback(interviewId, request).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Interview schedule not found with ID: " + interviewId, "INT_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Interview feedback and rating submitted successfully", response));
    }

    // ── 5. OFFERS ENDPOINTS ─────────────────────────────────────────────────
    @PostMapping("/recruitments/offers")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createOffer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody OfferRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        try {
            OfferResponse response = recruitmentService.createOffer(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Job offer generated and sent to candidate", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "OFF_001"));
        }
    }

    @PatchMapping("/recruitments/offers/{offerId}/status")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateOfferStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long offerId,
            @RequestBody Map<String, String> body){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Status field is required", "VAL_001"));
        }

        OfferResponse response = recruitmentService.updateOfferStatus(offerId, status).orElse(null);
        if (response == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Offer not found with ID: " + offerId, "OFF_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Offer status updated to " + status.toUpperCase(), response));
    }

    // ── 6. HIRE ENDPOINT ────────────────────────────────────────────────────
    @PostMapping("/recruitments/candidates/{candidateId}/hire")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> hireCandidate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long candidateId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        try {
            Employee employee = recruitmentService.hireCandidate(candidateId);
            return ResponseEntity.ok(ApiResponse.success(
                    "Candidate hired successfully! Created employee profile (" + employee.getEmployeeId() + ") and standard user account.",
                    employee));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "HIRE_001"));
        }
    }

    // ── 7. REPORTS ENDPOINT ─────────────────────────────────────────────────
    @GetMapping("/recruitments/reports/{reportType}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String reportType){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        Map<String, Object> data = recruitmentService.getReportData(reportType);
        return ResponseEntity.ok(ApiResponse.success("Recruitment report statistics generated successfully", data));
    }

    // ── 8. NOTIFICATIONS ENDPOINT ───────────────────────────────────────────
    @PostMapping("/recruitments/notifications/send")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> sendNotification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        Map<String, Object> response = recruitmentService.triggerNotification(request);
        return ResponseEntity.ok(ApiResponse.success("Recruitment alert notification sent successfully", response));
    }

    // ── 9. RESUME UPLOAD / DOWNLOAD ENDPOINTS ────────────────────────────────
    @PostMapping("/recruitments/candidates/{id}/resume")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> uploadResume(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        if (file.isEmpty()) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error("File is empty", "VAL_001"));
        }

        try {
            CandidateResponse updated = recruitmentService.uploadResume(
                    id, file.getOriginalFilename(), file.getContentType(), file.getBytes());
            return ResponseEntity.ok(ApiResponse.success("Candidate resume uploaded successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAND_002"));
        } catch (IOException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse.error("Error reading uploaded file: " + e.getMessage(), "SYS_500"));
        }
    }

    @GetMapping("/recruitments/candidates/{id}/resume")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<Object> getResume(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        Candidate candidate = recruitmentService.getResumeData(id).orElse(null);
        if (candidate == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Candidate not found with ID: " + id, "CAND_002"));
        }

        if (candidate.getResumeData() == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Resume file not found for candidate: " + candidate.getFullName(), "FILE_001"));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(candidate.getResumeFileType() != null ? candidate.getResumeFileType() : "application/pdf"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + candidate.getResumeFileName() + "\"")
                .body(candidate.getResumeData());
    }

    @Operation(summary = "Publish Job Posting", description = "Publishes a job posting, setting status to PUBLISHED.")
    @PostMapping("/recruitments/jobs/{jobId}/publish")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> publishJob(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long jobId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        JobResponse updated = recruitmentService.updateJobStatus(jobId, "PUBLISHED").orElse(null);
        if (updated == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Job not found with ID: " + jobId, "JOB_001"));
        }
        return ResponseEntity.ok(ApiResponse.success("Job published successfully", updated));
    }

    @Operation(summary = "Create Background Verification", description = "Registers background check information for a candidate.")
    @PostMapping("/recruitments/background-verification")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createBackgroundVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid BackgroundVerificationRequest request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        try {
            BackgroundVerification record = recruitmentService.createBackgroundVerification(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Background verification recorded successfully", record));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "BV_001"));
        }
    }

    @Operation(summary = "Get Background Verification", description = "Retrieves background check details by ID.")
    @GetMapping("/recruitments/background-verification/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getBackgroundVerification(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!checkRecruitmentPermission(currentUser)) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'recruitment.manage' permission.", "AUTH_002"));
        }

        BackgroundVerification record = recruitmentService.getBackgroundVerification(id).orElse(null);
        if (record == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Background verification record not found with ID: " + id, "BV_002"));
        }
        return ResponseEntity.ok(ApiResponse.success("Background verification record retrieved successfully", record));
    }
}
