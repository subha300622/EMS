package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.AppraisalResultResponseDto;
import com.example.ems.appraisal.dto.ReviewStageDto;
import com.example.ems.appraisal.dto.SelfAssessmentDto;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.AppraisalAssessmentRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.AppraisalReviewRepository;
import com.example.ems.appraisal.repository.AppraisalReviewStageRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.ems.appraisal.dto.AppraisalCurrentStageResponseDto;
import com.example.ems.appraisal.dto.EmployeePerformanceSummaryDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceStatus;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.goal.domain.Goal;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.performance.entity.PerformanceReviewRecord;
import com.example.ems.performance.repository.PerformanceReviewRecordRepository;
import org.springframework.beans.factory.annotation.Qualifier;

import com.example.ems.security.rls.PostgresRlsSessionBinder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;

@Service
public class AppraisalEvaluationService {

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalAssessmentRepository assessmentRepository;

    @Autowired
    private AppraisalReviewRepository reviewRepository;

    @Autowired
    private AppraisalReviewStageRepository reviewStageRepository;

    @Autowired
    private AppraisalHistoryService historyService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    @Qualifier("enterpriseGoalRepository")
    private GoalRepository goalRepository;

    @Autowired
    private PerformanceReviewRecordRepository performanceReviewRecordRepository;

    @Autowired(required = false)
    private PostgresRlsSessionBinder rlsSessionBinder;

    private void bindRlsIfAvailable() {
        if (rlsSessionBinder != null) {
            try {
                rlsSessionBinder.bindCurrentTenant();
            } catch (Exception ignored) {}
        }
    }

    @Transactional
    public SelfAssessmentDto saveSelfAssessment(Long appraisalId, SelfAssessmentDto dto, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationIdForUpdate(appraisalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        if (!appraisal.getEmployee().getId().equals(employee.getId())) {
            throw new SecurityException("You are not authorized to submit self-assessment for this appraisal.");
        }

        if (appraisal.getStatus() == AppraisalStatus.STAGE_REVIEW || appraisal.getStatus() == AppraisalStatus.COMPLETED || appraisal.getStatus() == AppraisalStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot edit or submit self-assessment for completed or published appraisal.");
        }

        if (dto.getOverallRating() == null) {
            throw new IllegalArgumentException("Overall rating is required");
        }
        if (dto.getOverallRating() < 1.0 || dto.getOverallRating() > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }

        AppraisalAssessment assessment = assessmentRepository.findByAppraisalId(appraisalId)
                .orElseGet(() -> {
                    AppraisalAssessment a = new AppraisalAssessment();
                    a.setAppraisal(appraisal);
                    a.setCreatedAt(LocalDateTime.now());
                    return a;
                });

        assessment.setOverallRating(dto.getOverallRating());
        assessment.setStrengths(dto.getStrengths());
        assessment.setAchievements(dto.getAchievements());
        assessment.setDevelopmentAreas(dto.getDevelopmentAreas());
        assessment.setUpdatedAt(LocalDateTime.now());

        AppraisalAssessment saved = assessmentRepository.save(assessment);

        // Update basic fields on appraisal as well
        appraisal.setSelfRating(dto.getOverallRating());
        appraisal.setSelfReview(dto.getAchievements());
        appraisal.setStatus(AppraisalStatus.SELF_ASSESSMENT);
        appraisal.setUpdatedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        return mapToAssessmentDto(saved);
    }

    @Transactional
    public SelfAssessmentDto submitSelfAssessment(Long appraisalId, SelfAssessmentDto dto, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationIdForUpdate(appraisalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        if (appraisal.getStatus() == AppraisalStatus.STAGE_REVIEW || appraisal.getStatus() == AppraisalStatus.COMPLETED || appraisal.getStatus() == AppraisalStatus.PUBLISHED) {
            throw new IllegalStateException("Self-assessment has already been submitted or completed.");
        }

        if (!appraisal.getEmployee().getId().equals(employee.getId())) {
            throw new SecurityException("You are not authorized to submit self-assessment for this appraisal.");
        }

        if (dto.getOverallRating() == null) {
            throw new IllegalArgumentException("Overall rating is required");
        }
        if (dto.getOverallRating() < 1.0 || dto.getOverallRating() > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }

        AppraisalAssessment assessment = assessmentRepository.findByAppraisalId(appraisalId)
                .orElseGet(() -> {
                    AppraisalAssessment a = new AppraisalAssessment();
                    a.setAppraisal(appraisal);
                    a.setCreatedAt(LocalDateTime.now());
                    return a;
                });

        assessment.setOverallRating(dto.getOverallRating());
        assessment.setStrengths(dto.getStrengths());
        assessment.setAchievements(dto.getAchievements());
        assessment.setDevelopmentAreas(dto.getDevelopmentAreas());
        assessment.setSubmittedAt(LocalDateTime.now());
        assessment.setUpdatedAt(LocalDateTime.now());
        assessment = assessmentRepository.save(assessment);

        AppraisalStatus oldStatus = appraisal.getStatus();
        appraisal.setSelfRating(dto.getOverallRating());
        appraisal.setSelfReview(dto.getAchievements());
        appraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        appraisal.setCurrentStageOrder(1);
        appraisal.setSelfReviewSubmittedAt(LocalDateTime.now());
        appraisal.setUpdatedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        historyService.recordHistory(
                appraisal,
                employee,
                "SELF_ASSESSMENT_SUBMITTED",
                oldStatus.name(),
                AppraisalStatus.STAGE_REVIEW.name(),
                employee,
                "Employee submitted self-assessment rating: " + dto.getOverallRating()
        );

        return mapToAssessmentDto(assessment);
    }

    @Transactional
    public ReviewStageDto submitReview(Long appraisalId, ReviewStageDto dto, Employee reviewer) {
        return submitReview(appraisalId, dto, reviewer, null);
    }

    @Transactional
    public ReviewStageDto submitReview(Long appraisalId, ReviewStageDto dto, Employee reviewer, User currentUser) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationIdForUpdate(appraisalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        if (appraisal.getEmployee().getId().equals(reviewer.getId())) {
            throw new SecurityException("Employee cannot review their own appraisal.");
        }

        if (appraisal.getStatus() == AppraisalStatus.CREATED) {
            throw new IllegalStateException("Cannot submit review before employee self-assessment is submitted.");
        }

        if (appraisal.getStatus() == AppraisalStatus.COMPLETED || appraisal.getStatus() == AppraisalStatus.PUBLISHED) {
            throw new IllegalStateException("Cannot submit review for completed or published appraisal.");
        }

        if (dto.getRating() == null) {
            throw new IllegalArgumentException("Rating is required");
        }
        if (dto.getRating() < 1.0 || dto.getRating() > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1.0 and 5.0");
        }

        // 1. Load configured review stages for this organization
        List<AppraisalReviewStage> configuredStages = reviewStageRepository.findByOrganizationIdOrderByStageOrderAsc(orgId);

        int currentStage = appraisal.getCurrentStageOrder() != null ? appraisal.getCurrentStageOrder() : 1;
        int requestedStage = dto.getStageOrder() != null ? dto.getStageOrder() : currentStage;

        if (requestedStage > currentStage) {
            throw new IllegalStateException("Cannot submit review for stage " + requestedStage + " while current stage is " + currentStage);
        }

        // 2. Identify target stage definition
        Optional<AppraisalReviewStage> matchingStageOpt = configuredStages.stream()
                .filter(s -> s.getStageOrder().equals(requestedStage))
                .findFirst();

        String stageName = dto.getStageName();
        String requiredPermission = "APPRAISAL_REVIEW";
        if (matchingStageOpt.isPresent()) {
            AppraisalReviewStage stageDef = matchingStageOpt.get();
            if (stageName == null || stageName.isBlank()) {
                stageName = stageDef.getStageName();
            }
            if (stageDef.getRequiredPermission() != null && !stageDef.getRequiredPermission().isBlank()) {
                requiredPermission = stageDef.getRequiredPermission();
            }
        } else if (stageName == null || stageName.isBlank()) {
            stageName = "Stage " + requestedStage + " Review";
        }

        // 3. Permission validation for authenticated user
        if (currentUser != null) {
            boolean isPlatformAdmin = currentUser.getRole() != null &&
                    ("PLATFORM_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()) || "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()));
            if (!isPlatformAdmin) {
                boolean hasSpecificPerm = roleService.hasPermission(currentUser.getWorkEmail(), requiredPermission);
                boolean hasGenericPerm = roleService.hasPermission(currentUser.getWorkEmail(), "APPRAISAL_REVIEW")
                        || roleService.hasPermission(currentUser.getWorkEmail(), "APPRAISAL_APPROVE");
                if (!hasSpecificPerm && !hasGenericPerm) {
                    throw new SecurityException("Access Denied: Missing required permission '" + requiredPermission + "' for review stage " + stageName);
                }
            }
        }

        final String finalStageName = stageName;
        AppraisalReview review = reviewRepository.findByAppraisalIdAndStageOrder(appraisalId, requestedStage)
                .orElseGet(() -> {
                    AppraisalReview r = new AppraisalReview();
                    r.setAppraisal(appraisal);
                    r.setStageOrder(requestedStage);
                    r.setCreatedAt(LocalDateTime.now());
                    return r;
                });

        review.setReviewer(reviewer);
        review.setStageName(finalStageName);
        review.setRating(dto.getRating());
        review.setComments(dto.getComments());
        review.setRecommendation(dto.getRecommendation());
        review.setSubmittedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        AppraisalReview savedReview = reviewRepository.save(review);

        // 4. Advance to next configured stage or mark COMPLETED
        Optional<AppraisalReviewStage> nextConfiguredStageOpt = configuredStages.stream()
                .filter(s -> s.getStageOrder() > requestedStage)
                .findFirst();

        // Calculate aggregate rating from all reviews
        List<AppraisalReview> allReviews = reviewRepository.findByAppraisalIdOrderByStageOrderAsc(appraisalId);
        double avgRating = allReviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToDouble(AppraisalReview::getRating)
                .average()
                .orElse(dto.getRating());

        appraisal.setFinalRating(Math.round(avgRating * 100.0) / 100.0);
        appraisal.setPerformanceCategory(determineCategory(appraisal.getFinalRating()));

        if (nextConfiguredStageOpt.isPresent()) {
            // More stages pending
            appraisal.setCurrentStageOrder(nextConfiguredStageOpt.get().getStageOrder());
            appraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        } else if (configuredStages.isEmpty() && requestedStage < 5) {
            // Default 5-stage flow fallback
            appraisal.setCurrentStageOrder(requestedStage + 1);
            appraisal.setStatus(AppraisalStatus.STAGE_REVIEW);
        } else {
            // All stages finished
            appraisal.setCurrentStageOrder(requestedStage + 1);
            appraisal.setStatus(AppraisalStatus.COMPLETED);
            appraisal.setCompletedAt(LocalDateTime.now());
        }

        appraisal.setUpdatedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        historyService.recordHistory(
                appraisal,
                appraisal.getEmployee(),
                "STAGE_REVIEW_SUBMITTED",
                "Stage " + requestedStage + " (" + finalStageName + ")",
                "Rating: " + dto.getRating() + ", Recommendation: " + dto.getRecommendation(),
                reviewer,
                dto.getComments()
        );

        return mapToReviewDto(savedReview);
    }

    @Transactional
    public AppraisalResultResponseDto publishAppraisal(Long appraisalId, Employee publisher) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        if (appraisal.getStatus() != AppraisalStatus.COMPLETED) {
            throw new IllegalStateException("Cannot publish appraisal before all review stages are completed. Current status: " + appraisal.getStatus());
        }

        if (appraisal.getPerformanceCategory() == null && appraisal.getFinalRating() != null) {
            appraisal.setPerformanceCategory(determineCategory(appraisal.getFinalRating()));
        }

        AppraisalStatus oldStatus = appraisal.getStatus();
        appraisal.setStatus(AppraisalStatus.PUBLISHED);
        appraisal.setPublishedAt(LocalDateTime.now());
        appraisal.setUpdatedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        historyService.recordHistory(
                appraisal,
                appraisal.getEmployee(),
                "APPRAISAL_PUBLISHED",
                oldStatus.name(),
                AppraisalStatus.PUBLISHED.name(),
                publisher,
                "Final appraisal published with rating " + appraisal.getFinalRating()
        );

        return getAppraisalResult(appraisalId);
    }

    @Transactional(readOnly = true)
    public AppraisalResultResponseDto getAppraisalResult(Long appraisalId) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        AppraisalResultResponseDto dto = new AppraisalResultResponseDto();
        dto.setAppraisalId(appraisal.getId());
        if (appraisal.getEmployee() != null) {
            dto.setEmployeeId(appraisal.getEmployee().getId());
            dto.setEmployeeName(appraisal.getEmployee().getFirstName() + " " + (appraisal.getEmployee().getLastName() != null ? appraisal.getEmployee().getLastName() : ""));
        }
        if (appraisal.getCycle() != null) {
            dto.setCycleId(appraisal.getCycle().getId());
            dto.setCycleName(appraisal.getCycle().getName());
        }
        if (appraisal.getRequest() != null) {
            dto.setRequestId(appraisal.getRequest().getId());
        }
        dto.setFinalRating(appraisal.getFinalRating());
        dto.setPerformanceCategory(appraisal.getPerformanceCategory());
        dto.setStatus(appraisal.getStatus());
        dto.setCompletedAt(appraisal.getCompletedAt());
        dto.setPublishedAt(appraisal.getPublishedAt());

        assessmentRepository.findByAppraisalId(appraisalId)
                .ifPresent(a -> dto.setSelfAssessment(mapToAssessmentDto(a)));

        List<ReviewStageDto> reviews = reviewRepository.findByAppraisalIdOrderByStageOrderAsc(appraisalId)
                .stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());
        dto.setReviews(reviews);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<AppraisalResultResponseDto> getEmployeeAppraisals(Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();
        return appraisalRepository.findByOrganizationIdAndEmployeeId(orgId, employeeId)
                .stream()
                .map(a -> getAppraisalResult(a.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SelfAssessmentDto getSelfAssessment(Long appraisalId) {
        Long orgId = TenantContext.requireOrganizationId();
        appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        return assessmentRepository.findByAppraisalId(appraisalId)
                .map(this::mapToAssessmentDto)
                .orElse(new SelfAssessmentDto());
    }

    @Transactional(readOnly = true)
    public AppraisalCurrentStageResponseDto getCurrentStage(Long appraisalId, User currentUser) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        List<AppraisalReviewStage> stages = reviewStageRepository.findByOrganizationIdOrderByStageOrderAsc(orgId);
        int currentOrder = appraisal.getCurrentStageOrder() != null ? appraisal.getCurrentStageOrder() : 1;

        Optional<AppraisalReviewStage> currentStageOpt = stages.stream()
                .filter(s -> s.getStageOrder().equals(currentOrder))
                .findFirst();

        String stageName = currentStageOpt.map(AppraisalReviewStage::getStageName).orElse("Stage " + currentOrder + " Review");
        String requiredPerm = currentStageOpt.map(AppraisalReviewStage::getRequiredPermission).orElse("APPRAISAL_REVIEW");
        boolean isRequired = currentStageOpt.map(AppraisalReviewStage::isRequired).orElse(true);
        Double weightage = currentStageOpt.map(AppraisalReviewStage::getWeightage).orElse(null);

        boolean isCompleted = appraisal.getStatus() == AppraisalStatus.COMPLETED || appraisal.getStatus() == AppraisalStatus.PUBLISHED;

        boolean canReview = false;
        if (!isCompleted && currentUser != null && appraisal.getEmployee() != null && !appraisal.getEmployee().getId().equals(currentUser.getId())) {
            boolean isPlatformAdmin = currentUser.getRole() != null &&
                    ("PLATFORM_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()) || "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()));
            if (isPlatformAdmin) {
                canReview = true;
            } else {
                canReview = roleService.hasPermission(currentUser.getWorkEmail(), requiredPerm)
                        || roleService.hasPermission(currentUser.getWorkEmail(), "APPRAISAL_REVIEW")
                        || roleService.hasPermission(currentUser.getWorkEmail(), "APPRAISAL_APPROVE");
            }
        }

        List<ReviewStageDto> completedReviews = reviewRepository.findByAppraisalIdOrderByStageOrderAsc(appraisalId)
                .stream()
                .map(this::mapToReviewDto)
                .collect(Collectors.toList());

        AppraisalCurrentStageResponseDto response = new AppraisalCurrentStageResponseDto();
        response.setAppraisalId(appraisal.getId());
        if (appraisal.getEmployee() != null) {
            response.setEmployeeId(appraisal.getEmployee().getId());
            response.setEmployeeName(appraisal.getEmployee().getFullName());
        }
        if (appraisal.getCycle() != null) {
            response.setCycleId(appraisal.getCycle().getId());
            response.setCycleName(appraisal.getCycle().getName());
        }
        response.setAppraisalStatus(appraisal.getStatus() != null ? appraisal.getStatus().name() : "UNKNOWN");
        response.setCurrentStageOrder(currentOrder);
        response.setCurrentStageName(stageName);
        response.setRequiredPermission(requiredPerm);
        response.setRequired(isRequired);
        response.setWeightage(weightage);
        response.setCanReview(canReview);
        response.setCompleted(isCompleted);
        response.setCompletedReviews(completedReviews);
        return response;
    }

    @Transactional(readOnly = true)
    public List<AppraisalResultResponseDto> getPendingReviews(User currentUser) {
        Long orgId = TenantContext.requireOrganizationId();
        List<Appraisal> activeAppraisals = appraisalRepository.findByOrganizationId(orgId).stream()
                .filter(a -> a.getStatus() == AppraisalStatus.STAGE_REVIEW || a.getStatus() == AppraisalStatus.CREATED || a.getStatus() == AppraisalStatus.SELF_ASSESSMENT)
                .collect(Collectors.toList());

        List<AppraisalReviewStage> stages = reviewStageRepository.findByOrganizationIdOrderByStageOrderAsc(orgId);

        boolean isPlatformAdmin = currentUser != null && currentUser.getRole() != null &&
                ("PLATFORM_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()) || "SUPER_ADMIN".equalsIgnoreCase(currentUser.getRole().getName()));

        return activeAppraisals.stream()
                .filter(a -> {
                    if (currentUser != null && a.getEmployee() != null && a.getEmployee().getEmail() != null
                            && a.getEmployee().getEmail().equalsIgnoreCase(currentUser.getWorkEmail())) {
                        return false; // Cannot review own appraisal
                    }
                    if (isPlatformAdmin) {
                        return true;
                    }
                    int curOrder = a.getCurrentStageOrder() != null ? a.getCurrentStageOrder() : 1;
                    String reqPerm = stages.stream()
                            .filter(s -> s.getStageOrder().equals(curOrder))
                            .map(AppraisalReviewStage::getRequiredPermission)
                            .findFirst()
                            .orElse("APPRAISAL_REVIEW");

                    return roleService.hasPermission(currentUser.getWorkEmail(), reqPerm)
                            || roleService.hasPermission(currentUser.getWorkEmail(), "APPRAISAL_REVIEW")
                            || roleService.hasPermission(currentUser.getWorkEmail(), "APPRAISAL_APPROVE");
                })
                .map(a -> getAppraisalResult(a.getId()))
                .collect(Collectors.toList());
    }

    private String determineCategory(Double rating) {
        if (rating == null) return "NOT_RATED";
        if (rating >= 4.5) return "OUTSTANDING";
        if (rating >= 3.8) return "EXCEEDS_EXPECTATIONS";
        if (rating >= 3.0) return "MEETS_EXPECTATIONS";
        if (rating >= 2.0) return "NEEDS_IMPROVEMENT";
        return "UNSATISFACTORY";
    }

    public SelfAssessmentDto mapToAssessmentDto(AppraisalAssessment a) {
        return new SelfAssessmentDto(
                a.getOverallRating(),
                a.getStrengths(),
                a.getAchievements(),
                a.getDevelopmentAreas(),
                a.getSubmittedAt()
        );
    }

    public ReviewStageDto mapToReviewDto(AppraisalReview r) {
        ReviewStageDto dto = new ReviewStageDto();
        dto.setId(r.getId());
        if (r.getReviewer() != null) {
            dto.setReviewerId(r.getReviewer().getId());
            dto.setReviewerName(r.getReviewer().getFirstName() + " " + (r.getReviewer().getLastName() != null ? r.getReviewer().getLastName() : ""));
        }
        dto.setStageOrder(r.getStageOrder());
        dto.setStageName(r.getStageName());
        dto.setRating(r.getRating());
        dto.setComments(r.getComments());
        dto.setRecommendation(r.getRecommendation());
        dto.setSubmittedAt(r.getSubmittedAt());
        return dto;
    }

    @Transactional(readOnly = true)
    public EmployeePerformanceSummaryDto getEmployeePerformanceSummary(Long employeeId) {
        Long organizationId = TenantContext.requireOrganizationId();
        bindRlsIfAvailable();
        Employee employee = employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        List<Appraisal> appraisals = appraisalRepository.findByOrganizationIdAndEmployeeId(organizationId, employeeId);

        // 1. Previous Appraisals: terminal statuses only (COMPLETED, PUBLISHED, CLOSED)
        List<EmployeePerformanceSummaryDto.PreviousAppraisalDto> previousAppraisals = appraisals.stream()
                .filter(a -> a.getStatus() == AppraisalStatus.COMPLETED ||
                             a.getStatus() == AppraisalStatus.PUBLISHED ||
                             a.getStatus() == AppraisalStatus.CLOSED)
                .sorted((a1, a2) -> {
                    LocalDateTime t1 = a1.getCompletedAt() != null ? a1.getCompletedAt() : a1.getPublishedAt();
                    LocalDateTime t2 = a2.getCompletedAt() != null ? a2.getCompletedAt() : a2.getPublishedAt();
                    if (t1 != null && t2 != null) {
                        return t2.compareTo(t1);
                    }
                    return a2.getId().compareTo(a1.getId());
                })
                .map(a -> {
                    String cycleName = a.getCycle() != null ? a.getCycle().getName() : "Appraisal #" + a.getId();
                    Integer cycleYear = null;
                    if (a.getCycle() != null && a.getCycle().getStartDate() != null) {
                        cycleYear = a.getCycle().getStartDate().getYear();
                    } else if (a.getCompletedAt() != null) {
                        cycleYear = a.getCompletedAt().getYear();
                    }
                    String ratingCategory = a.getPerformanceCategory() != null ? a.getPerformanceCategory()
                            : (a.getFinalRating() != null ? determineCategory(a.getFinalRating()) : null);
                    LocalDateTime completedTime = a.getCompletedAt() != null ? a.getCompletedAt() : a.getPublishedAt();
                    return new EmployeePerformanceSummaryDto.PreviousAppraisalDto(
                            a.getId(),
                            cycleName,
                            cycleYear,
                            a.getFinalRating(),
                            5.0,
                            "5_POINT",
                            ratingCategory,
                            completedTime
                    );
                })
                .collect(Collectors.toList());

        // 1b. Support for new Enterprise Performance review records (0-100 scale)
        List<PerformanceReviewRecord> perfRecords = performanceReviewRecordRepository.findByOrganizationIdAndEmployeeId(organizationId, employeeId);
        List<EmployeePerformanceSummaryDto.PreviousAppraisalDto> enterprisePrevAppraisals = perfRecords.stream()
                .filter(r -> "PUBLISHED".equalsIgnoreCase(r.getStatus()) ||
                             "LOCKED".equalsIgnoreCase(r.getStatus()) ||
                             "COMPLETED".equalsIgnoreCase(r.getStatus()))
                .map(r -> {
                    String cycleName = r.getCycle() != null ? r.getCycle().getName() : "Performance Review #" + r.getId();
                    Integer cycleYear = null;
                    if (r.getCycle() != null && r.getCycle().getStartDate() != null) {
                        cycleYear = r.getCycle().getStartDate().getYear();
                    } else if (r.getApprovedAt() != null) {
                        cycleYear = r.getApprovedAt().getYear();
                    }
                    Double finalScore = r.getFinalScore() != null ? r.getFinalScore().doubleValue()
                            : (r.getCalculatedScore() != null ? r.getCalculatedScore().doubleValue() : null);
                    String ratingBand = r.getRatingBand() != null ? r.getRatingBand() : "NOT_RATED";
                    LocalDateTime completedTime = r.getApprovedAt() != null ? r.getApprovedAt() : r.getSubmittedAt();
                    return new EmployeePerformanceSummaryDto.PreviousAppraisalDto(
                            r.getId(),
                            cycleName,
                            cycleYear,
                            finalScore,
                            100.0,
                            "100_POINT",
                            ratingBand,
                            completedTime
                    );
                })
                .collect(Collectors.toList());

        previousAppraisals.addAll(enterprisePrevAppraisals);
        previousAppraisals.sort((p1, p2) -> {
            if (p1.getCompletedAt() != null && p2.getCompletedAt() != null) {
                return p2.getCompletedAt().compareTo(p1.getCompletedAt());
            }
            return p2.getAppraisalId().compareTo(p1.getAppraisalId());
        });

        // 2. Current Review Context
        Appraisal currentAppraisal = appraisals.stream()
                .filter(a -> a.getStatus() != AppraisalStatus.COMPLETED &&
                             a.getStatus() != AppraisalStatus.PUBLISHED &&
                             a.getStatus() != AppraisalStatus.CLOSED &&
                             a.getStatus() != AppraisalStatus.CANCELLED)
                .max(Comparator.comparing(Appraisal::getId))
                .orElse(null);

        Optional<PerformanceReviewRecord> activePerfRecord = perfRecords.stream()
                .filter(r -> !"PUBLISHED".equalsIgnoreCase(r.getStatus()) &&
                             !"LOCKED".equalsIgnoreCase(r.getStatus()) &&
                             !"COMPLETED".equalsIgnoreCase(r.getStatus()) &&
                             !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .max(Comparator.comparing(PerformanceReviewRecord::getId));

        EmployeePerformanceSummaryDto.ReviewContextDto reviewContext = new EmployeePerformanceSummaryDto.ReviewContextDto();
        reviewContext.setPreviousReviewAvailable(!previousAppraisals.isEmpty());
        if (currentAppraisal != null) {
            reviewContext.setCurrentReviewId(currentAppraisal.getId());
            reviewContext.setCurrentCycleName(currentAppraisal.getCycle() != null ? currentAppraisal.getCycle().getName() : null);
            reviewContext.setCurrentStageOrder(currentAppraisal.getCurrentStageOrder() != null ? currentAppraisal.getCurrentStageOrder() : 1);
            reviewContext.setAppraisalStatus(currentAppraisal.getStatus() != null ? currentAppraisal.getStatus().name() : null);
        } else if (activePerfRecord.isPresent()) {
            PerformanceReviewRecord r = activePerfRecord.get();
            reviewContext.setCurrentReviewId(r.getId());
            reviewContext.setCurrentCycleName(r.getCycle() != null ? r.getCycle().getName() : null);
            reviewContext.setCurrentStageOrder(1);
            reviewContext.setAppraisalStatus(r.getStatus());
        }

        // 3. Period Start & Period End
        LocalDate periodStart = null;
        LocalDate periodEnd = null;
        if (currentAppraisal != null && currentAppraisal.getCycle() != null) {
            periodStart = currentAppraisal.getCycle().getStartDate();
            periodEnd = currentAppraisal.getCycle().getEndDate();
        } else if (activePerfRecord.isPresent() && activePerfRecord.get().getCycle() != null) {
            periodStart = activePerfRecord.get().getCycle().getStartDate();
            periodEnd = activePerfRecord.get().getCycle().getEndDate();
        }
        if (periodStart == null) {
            periodStart = LocalDate.now().withDayOfYear(1);
        }
        if (periodEnd == null || periodEnd.isAfter(LocalDate.now())) {
            periodEnd = LocalDate.now();
        }
        if (periodEnd.isBefore(periodStart)) {
            periodEnd = periodStart;
        }

        // 4. Attendance Metrics
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndDateBetweenAndOrganizationId(employeeId, periodStart, periodEnd, organizationId);
        int calculatedWorkingDays = 0;
        LocalDate cur = periodStart;
        while (!cur.isAfter(periodEnd)) {
            DayOfWeek dow = cur.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                calculatedWorkingDays++;
            }
            cur = cur.plusDays(1);
        }
        int workingDays = Math.max(1, calculatedWorkingDays);

        double presentCount = 0.0;
        for (Attendance att : attendances) {
            AttendanceStatus aStatus = att.getAttendanceStatus();
            if (aStatus == AttendanceStatus.PRESENT ||
                aStatus == AttendanceStatus.LATE ||
                aStatus == AttendanceStatus.WORKING ||
                aStatus == AttendanceStatus.COMPLETED) {
                presentCount += 1.0;
            } else if (aStatus == AttendanceStatus.HALF_DAY) {
                presentCount += 0.5;
            }
        }
        int presentDays = (int) Math.round(presentCount);
        if (presentDays > workingDays) {
            workingDays = presentDays;
        }
        Double attendancePercentage = workingDays > 0
                ? BigDecimal.valueOf((presentCount / workingDays) * 100.0).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 5. Approved Leaves
        List<Leave> leaves = leaveRepository.findByEmployeeIdAndStatus(employeeId, "APPROVED");
        double totalLeaveDays = 0.0;
        for (Leave l : leaves) {
            if (l.getOrganization() != null && !organizationId.equals(l.getOrganization().getId())) {
                continue;
            }
            if (l.getStartDate() != null && l.getEndDate() != null) {
                if (!l.getStartDate().isAfter(periodEnd) && !l.getEndDate().isBefore(periodStart)) {
                    LocalDate clampedStart = l.getStartDate().isBefore(periodStart) ? periodStart : l.getStartDate();
                    LocalDate clampedEnd = l.getEndDate().isAfter(periodEnd) ? periodEnd : l.getEndDate();
                    long totalSpan = java.time.temporal.ChronoUnit.DAYS.between(l.getStartDate(), l.getEndDate()) + 1;
                    long clampedSpan = java.time.temporal.ChronoUnit.DAYS.between(clampedStart, clampedEnd) + 1;
                    double leaveDuration = l.getDurationDays() != null ? l.getDurationDays() : (double) totalSpan;
                    double proportionalDays = totalSpan > 0 ? (clampedSpan * leaveDuration) / totalSpan : leaveDuration;
                    totalLeaveDays += proportionalDays;
                }
            }
        }
        int leaveDays = (int) Math.round(totalLeaveDays);

        // 6. Goals Projection
        List<Goal> goals = goalRepository.findByOrganizationIdAndOwnerIdAndIsDeletedFalse(organizationId, employeeId);
        int totalGoals = goals.size();
        int completedGoals = 0;
        for (Goal g : goals) {
            if ("COMPLETED".equalsIgnoreCase(g.getStatus()) || (g.getProgress() != null && g.getProgress() >= 100)) {
                completedGoals++;
            }
        }
        Double goalCompletionPercentage = totalGoals > 0
                ? BigDecimal.valueOf(((double) completedGoals / totalGoals) * 100.0).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        // 7. KPI Achievement Percentage
        Double kpiAchievementPercentage = null;
        if (currentAppraisal != null && currentAppraisal.getCycle() != null) {
            Optional<PerformanceReviewRecord> reviewRecordOpt = performanceReviewRecordRepository
                    .findByOrganizationIdAndCycleIdAndEmployeeId(organizationId, currentAppraisal.getCycle().getId(), employeeId);
            if (reviewRecordOpt.isPresent()) {
                PerformanceReviewRecord rec = reviewRecordOpt.get();
                if (rec.getCalculatedAt() != null && rec.getKpiWeightedScore() != null) {
                    kpiAchievementPercentage = rec.getKpiWeightedScore().setScale(2, RoundingMode.HALF_UP).doubleValue();
                }
            }
        }
        if (kpiAchievementPercentage == null && activePerfRecord.isPresent()) {
            PerformanceReviewRecord rec = activePerfRecord.get();
            if (rec.getCalculatedAt() != null && rec.getKpiWeightedScore() != null) {
                kpiAchievementPercentage = rec.getKpiWeightedScore().setScale(2, RoundingMode.HALF_UP).doubleValue();
            }
        }

        // 8. Assemble Full Summary
        EmployeePerformanceSummaryDto dto = new EmployeePerformanceSummaryDto();
        dto.setEmployeeId(employee.getId());
        dto.setEmployeeName(employee.getFullName());
        dto.setEmployeeCode(employee.getEmployeeId());
        dto.setDepartment(employee.getDepartment());
        dto.setDesignation(employee.getDesignation());
        dto.setPreviousAppraisals(previousAppraisals);

        EmployeePerformanceSummaryDto.CurrentPeriodPerformanceDto currentPeriod = new EmployeePerformanceSummaryDto.CurrentPeriodPerformanceDto();
        currentPeriod.setPeriodStart(periodStart);
        currentPeriod.setPeriodEnd(periodEnd);
        currentPeriod.setAttendancePercentage(attendancePercentage);
        currentPeriod.setWorkingDays(workingDays);
        currentPeriod.setPresentDays(presentDays);
        currentPeriod.setLeaveDays(leaveDays);
        currentPeriod.setTotalGoals(totalGoals);
        currentPeriod.setCompletedGoals(completedGoals);
        currentPeriod.setGoalCompletionPercentage(goalCompletionPercentage);
        currentPeriod.setKpiAchievementPercentage(kpiAchievementPercentage);
        dto.setCurrentPeriodPerformance(currentPeriod);

        dto.setReviewContext(reviewContext);

        return dto;
    }
}

