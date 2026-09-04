package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.recruitment.dto.*;
import com.example.ems.recruitment.entity.*;
import com.example.ems.recruitment.repository.*;
import com.example.ems.security.context.TenantContext;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private InterviewRepository interviewRepository;

    @Autowired
    private AuditLogService auditLogService;

    public ApplicationResponse applyForJob(Long jobId, JobApplicationRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BadRequestException("This job position is no longer accepting applications");
        }

        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("The application deadline for this position has passed");
        }

        Long orgId = job.getOrganizationId();

        Optional<Candidate> existingCandidate = candidateRepository.findByOrganizationIdAndEmailOrPhone(
                orgId, request.getEmail(), request.getPhone());

        Candidate candidate;
        if (existingCandidate.isPresent()) {
            candidate = existingCandidate.get();

            boolean alreadyApplied = applicationRepository.existsByOrganizationIdAndCandidateIdAndJobId(
                    orgId, candidate.getId(), jobId);

            if (alreadyApplied) {
                throw new ConflictException("ALREADY_APPLIED: You have already applied for this position");
            }

            candidate.setFullName(request.getFullName());
            candidate.setExperienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : candidate.getExperienceYears());
            if (request.getCurrentCompany() != null) candidate.setCurrentCompany(request.getCurrentCompany());
            if (request.getCurrentDesignation() != null) candidate.setCurrentDesignation(request.getCurrentDesignation());
            if (request.getExpectedSalary() != null) candidate.setExpectedSalary(request.getExpectedSalary());
            if (request.getNoticePeriodDays() != null) candidate.setNoticePeriodDays(request.getNoticePeriodDays());
            if (request.getCoverLetter() != null) candidate.setCoverLetter(request.getCoverLetter());
            if (request.getResumeUrl() != null) candidate.setResumeUrl(request.getResumeUrl());
            candidate = candidateRepository.save(candidate);
        } else {
            candidate = new Candidate();
            candidate.setOrganizationId(orgId);
            candidate.setFullName(request.getFullName());
            candidate.setEmail(request.getEmail());
            candidate.setPhone(request.getPhone());
            candidate.setExperienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0.0);
            candidate.setCurrentCompany(request.getCurrentCompany());
            candidate.setCurrentDesignation(request.getCurrentDesignation());
            candidate.setExpectedSalary(request.getExpectedSalary());
            candidate.setNoticePeriodDays(request.getNoticePeriodDays());
            candidate.setCoverLetter(request.getCoverLetter());
            candidate.setResumeUrl(request.getResumeUrl());
            candidate.setTalentPoolStatus(TalentPoolStatus.AVAILABLE);
            candidate = candidateRepository.save(candidate);
        }

        Application application = new Application();
        application.setOrganizationId(orgId);
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setApplicationNumber("APP-" + System.currentTimeMillis() + "-" + (1000 + new Random().nextInt(9000)));

        Application savedApp = applicationRepository.save(application);

        recordStatusHistory(savedApp, null, ApplicationStatus.APPLIED, "CANDIDATE", "Candidate submitted application");

        auditLogService.logAction("PUBLIC_USER", request.getEmail(), "APPLY_JOB", "Application",
                savedApp.getId().toString(), "127.0.0.1", "Candidate " + candidate.getFullName() + " applied for job " + job.getTitle());

        return new ApplicationResponse(savedApp);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getHRApplications(
            Long jobId, ApplicationStatus status, Long departmentId, String location,
            Double experienceMin, Double experienceMax, String search, Pageable pageable) {

        Long orgId = TenantContext.requireOrganizationId();

        Specification<Application> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), orgId));

            if (jobId != null) {
                predicates.add(cb.equal(root.get("job").get("id"), jobId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (departmentId != null) {
                predicates.add(cb.equal(root.get("job").get("departmentId"), departmentId));
            }
            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("job").get("location")), "%" + location.toLowerCase() + "%"));
            }
            if (experienceMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("candidate").get("experienceYears"), experienceMin));
            }
            if (experienceMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("candidate").get("experienceYears"), experienceMax));
            }
            if (search != null && !search.isBlank()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("candidate").get("fullName")), searchLike);
                Predicate emailMatch = cb.like(cb.lower(root.get("candidate").get("email")), searchLike);
                Predicate phoneMatch = cb.like(cb.lower(root.get("candidate").get("phone")), searchLike);
                Predicate appNumMatch = cb.like(cb.lower(root.get("applicationNumber")), searchLike);
                predicates.add(cb.or(nameMatch, emailMatch, phoneMatch, appNumMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return applicationRepository.findAll(spec, pageable).map(ApplicationResponse::new);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long applicationId) {
        Long orgId = TenantContext.requireOrganizationId();
        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));
        return new ApplicationResponse(app);
    }

    public ApplicationResponse shortlistCandidate(Long applicationId) {
        Long orgId = TenantContext.requireOrganizationId();
        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (!app.getStatus().isValidTransition(ApplicationStatus.SHORTLISTED)) {
            throw new BadRequestException("Cannot transition application status from " + app.getStatus() + " to SHORTLISTED");
        }

        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(ApplicationStatus.SHORTLISTED);
        Application updated = applicationRepository.save(app);

        recordStatusHistory(updated, oldStatus, ApplicationStatus.SHORTLISTED, "HR", "Shortlisted for interview process");

        auditLogService.logAction("HR", "hr@company.com", "SHORTLIST_CANDIDATE", "Application",
                updated.getId().toString(), "127.0.0.1", "Application " + updated.getApplicationNumber() + " shortlisted");

        return new ApplicationResponse(updated);
    }

    public ApplicationResponse rejectCandidate(Long applicationId, String reason) {
        Long orgId = TenantContext.requireOrganizationId();
        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (!app.getStatus().isValidTransition(ApplicationStatus.REJECTED)) {
            throw new BadRequestException("Cannot transition application status from " + app.getStatus() + " to REJECTED");
        }

        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(ApplicationStatus.REJECTED);
        app.setRejectionReason(reason);
        Application updated = applicationRepository.save(app);

        Candidate candidate = app.getCandidate();
        if (candidate != null) {
            candidate.setTalentPoolStatus(TalentPoolStatus.AVAILABLE);
            candidateRepository.save(candidate);
        }

        recordStatusHistory(updated, oldStatus, ApplicationStatus.REJECTED, "HR", reason);

        auditLogService.logAction("HR", "hr@company.com", "REJECT_CANDIDATE", "Application",
                updated.getId().toString(), "127.0.0.1", "Application " + updated.getApplicationNumber() + " rejected. Reason: " + reason);

        return new ApplicationResponse(updated);
    }

    public ApplicationResponse selectCandidate(Long applicationId) {
        Long orgId = TenantContext.requireOrganizationId();
        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (!app.getStatus().isValidTransition(ApplicationStatus.SELECTED)) {
            throw new BadRequestException("Cannot transition application status from " + app.getStatus() + " to SELECTED");
        }

        List<Interview> interviews = interviewRepository.findByOrganizationIdAndApplicationId(orgId, applicationId);
        boolean hasCompletedInterviewWithPositiveFeedback = interviews.stream().anyMatch(i ->
                i.getStatus() == InterviewStatus.COMPLETED &&
                i.getRecommendation() != null &&
                (i.getRecommendation() == InterviewRecommendation.SELECT || i.getRecommendation() == InterviewRecommendation.NEXT_ROUND));

        if (!hasCompletedInterviewWithPositiveFeedback) {
            throw new BadRequestException("Candidate cannot be selected without completing required interview process with feedback");
        }

        ApplicationStatus oldStatus = app.getStatus();
        app.setStatus(ApplicationStatus.SELECTED);
        Application updated = applicationRepository.save(app);

        recordStatusHistory(updated, oldStatus, ApplicationStatus.SELECTED, "HR", "Candidate selected after interview evaluation");

        auditLogService.logAction("HR", "hr@company.com", "SELECT_CANDIDATE", "Application",
                updated.getId().toString(), "127.0.0.1", "Candidate selected for application " + updated.getApplicationNumber());

        return new ApplicationResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<ApplicationStatusHistoryResponse> getStatusHistory(Long applicationId) {
        Long orgId = TenantContext.requireOrganizationId();
        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        return statusHistoryRepository.findByApplicationIdOrderByCreatedAtDesc(app.getId()).stream()
                .map(ApplicationStatusHistoryResponse::new)
                .collect(Collectors.toList());
    }

    public void recordStatusHistory(Application app, ApplicationStatus oldStatus, ApplicationStatus newStatus, String changedBy, String reason) {
        ApplicationStatusHistory history = new ApplicationStatusHistory();
        history.setApplication(app);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setReason(reason);
        statusHistoryRepository.save(history);
    }
}
