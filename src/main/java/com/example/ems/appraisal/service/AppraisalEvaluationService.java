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
}

