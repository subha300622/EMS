package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.recruitment.dto.JobCreateRequest;
import com.example.ems.recruitment.dto.JobResponse;
import com.example.ems.recruitment.dto.JobUpdateRequest;
import com.example.ems.recruitment.dto.PublicJobResponse;
import com.example.ems.recruitment.entity.Job;
import com.example.ems.recruitment.entity.JobStatus;
import com.example.ems.recruitment.repository.ApplicationRepository;
import com.example.ems.recruitment.repository.JobRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AuditLogService auditLogService;

    @org.springframework.beans.factory.annotation.Value("${app.frontend-url}")
    private String frontendUrl;

    public JobResponse createJob(JobCreateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
            if (department.getOrganization() != null && !orgId.equals(department.getOrganization().getId())) {
                throw new BadRequestException("Department does not belong to your organization");
            }
            if (request.getDepartment() == null || request.getDepartment().isBlank()) {
                request.setDepartment(department.getName());
            }
        }

        if (request.getExperienceMin() != null && request.getExperienceMax() != null) {
            if (request.getExperienceMin() > request.getExperienceMax()) {
                throw new BadRequestException("Minimum experience cannot be greater than maximum experience");
            }
        }

        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            if (request.getSalaryMin().compareTo(request.getSalaryMax()) > 0) {
                throw new BadRequestException("Minimum salary cannot be greater than maximum salary");
            }
        }

        if (request.getApplicationDeadline() != null && request.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("Application deadline cannot be in the past");
        }

        if (request.getOpenings() != null && request.getOpenings() <= 0) {
            throw new BadRequestException("Openings must be greater than zero");
        }

        Job job = new Job();
        job.setOrganizationId(orgId);
        job.setDepartmentId(request.getDepartmentId());
        job.setDepartment(request.getDepartment());
        job.setTitle(request.getTitle());
        job.setLocation(request.getLocation());
        job.setEmploymentType(request.getEmploymentType());
        job.setExperienceMin(request.getExperienceMin() != null ? request.getExperienceMin() : 0);
        job.setExperienceMax(request.getExperienceMax() != null ? request.getExperienceMax() : 0);
        job.setOpenings(request.getOpenings() != null ? request.getOpenings() : 1);
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());

        if (request.getSalaryMin() != null && request.getSalaryMax() != null) {
            job.setSalaryRange(request.getSalaryMin() + " - " + request.getSalaryMax());
        }

        job.setDescription(request.getDescription());

        if (request.getRequirements() != null && !request.getRequirements().isEmpty()) {
            job.setRequirements(String.join(", ", request.getRequirements()));
        }

        job.setApplicationDeadline(request.getApplicationDeadline());
        job.setStatus(JobStatus.DRAFT);

        Job saved = jobRepository.save(job);
        saved.setSlug(generateSlug(saved.getTitle(), saved.getId()));
        saved = jobRepository.save(saved);

        auditLogService.logAction("HR", "hr@company.com", "CREATE_JOB", "Job", saved.getId().toString(),
                getCurrentClientIp(), "Job created with ID: " + saved.getId() + ", title: " + saved.getTitle());

        return new JobResponse(saved, frontendUrl);
    }

    public JobResponse updateJob(Long jobId, JobUpdateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        Job job = jobRepository.findByOrganizationIdAndId(orgId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Closed jobs cannot be modified");
        }

        if (job.getStatus() == JobStatus.DRAFT) {
            if (request.getTitle() != null && !request.getTitle().isBlank()) {
                job.setTitle(request.getTitle());
                job.setSlug(generateSlug(request.getTitle(), job.getId()));
            }
            if (request.getDepartmentId() != null) {
                Department dept = departmentRepository.findById(request.getDepartmentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
                if (dept.getOrganization() != null && !orgId.equals(dept.getOrganization().getId())) {
                    throw new BadRequestException("Department does not belong to your organization");
                }
                job.setDepartmentId(request.getDepartmentId());
                job.setDepartment(dept.getName());
            } else if (request.getDepartment() != null) {
                job.setDepartment(request.getDepartment());
            }
            if (request.getEmploymentType() != null) {
                job.setEmploymentType(request.getEmploymentType());
            }
        }

        if (request.getLocation() != null) job.setLocation(request.getLocation());
        if (request.getExperienceMin() != null) job.setExperienceMin(request.getExperienceMin());
        if (request.getExperienceMax() != null) job.setExperienceMax(request.getExperienceMax());
        if (request.getOpenings() != null) job.setOpenings(request.getOpenings());
        if (request.getSalaryMin() != null) job.setSalaryMin(request.getSalaryMin());
        if (request.getSalaryMax() != null) job.setSalaryMax(request.getSalaryMax());

        if (job.getExperienceMin() > job.getExperienceMax()) {
            throw new BadRequestException("Minimum experience cannot be greater than maximum experience");
        }

        if (job.getSalaryMin() != null && job.getSalaryMax() != null) {
            if (job.getSalaryMin().compareTo(job.getSalaryMax()) > 0) {
                throw new BadRequestException("Minimum salary cannot be greater than maximum salary");
            }
            job.setSalaryRange(job.getSalaryMin() + " - " + job.getSalaryMax());
        }

        if (request.getDescription() != null) job.setDescription(request.getDescription());
        if (request.getRequirements() != null) {
            job.setRequirements(String.join(", ", request.getRequirements()));
        }
        if (request.getApplicationDeadline() != null) {
            if (request.getApplicationDeadline().isBefore(LocalDate.now())) {
                throw new BadRequestException("Application deadline cannot be in the past");
            }
            job.setApplicationDeadline(request.getApplicationDeadline());
        }

        Job updated = jobRepository.save(job);

        auditLogService.logAction("HR", "hr@company.com", "UPDATE_JOB", "Job", updated.getId().toString(),
                getCurrentClientIp(), "Job updated with ID: " + updated.getId());

        return new JobResponse(updated, frontendUrl);
    }

    public JobResponse publishJob(Long jobId) {
        Long orgId = TenantContext.requireOrganizationId();
        Job job = jobRepository.findByOrganizationIdAndId(orgId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (job.getStatus() != JobStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT jobs can be published");
        }

        validateJobForPublishing(job);

        job.setStatus(JobStatus.PUBLISHED);
        job.setPublishedAt(LocalDateTime.now());
        if (job.getSlug() == null || job.getSlug().isBlank()) {
            job.setSlug(generateSlug(job.getTitle(), job.getId()));
        }

        Job published = jobRepository.save(job);

        auditLogService.logAction("HR", "hr@company.com", "PUBLISH_JOB", "Job", published.getId().toString(),
                getCurrentClientIp(), "Job published with ID: " + published.getId());

        return new JobResponse(published, frontendUrl);
    }

    public List<JobResponse> getHRJobs() {
        Long orgId = TenantContext.requireOrganizationId();
        return jobRepository.findByOrganizationId(orgId).stream()
                .map(j -> new JobResponse(j, frontendUrl))
                .collect(Collectors.toList());
    }

    public JobResponse getHRJobById(Long jobId) {
        Long orgId = TenantContext.requireOrganizationId();
        Job job = jobRepository.findByOrganizationIdAndId(orgId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));
        return new JobResponse(job, frontendUrl);
    }

    @Transactional(readOnly = true)
    public List<PublicJobResponse> getPublicJobsForCompany(String companySlug) {
        Organization organization = organizationRepository.findByOrganizationCode(companySlug)
                .or(() -> {
                    try {
                        Long orgId = Long.parseLong(companySlug);
                        return organizationRepository.findById(orgId);
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with code: " + companySlug));

        if (organization.getStatus() != null && !"ACTIVE".equalsIgnoreCase(organization.getStatus().name())) {
            throw new BadRequestException("Company is currently inactive");
        }

        LocalDate now = LocalDate.now();
        List<Job> publishedJobs = jobRepository.findByOrganizationIdAndStatus(organization.getId(), JobStatus.PUBLISHED);

        return publishedJobs.stream()
                .filter(j -> j.getApplicationDeadline() == null || !j.getApplicationDeadline().isBefore(now))
                .map(j -> new PublicJobResponse(j, frontendUrl))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicJobResponse getPublicJob(String jobSlug) {
        Job job = jobRepository.findBySlugAndStatus(jobSlug, JobStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDate.now())) {
            throw new BadRequestException("Application deadline has passed for this position");
        }

        return new PublicJobResponse(job, frontendUrl);
    }

    @Transactional(readOnly = true)
    public PublicJobResponse getPublicJobBySlug(String jobSlug) {
        return getPublicJob(jobSlug);
    }

    public JobResponse closeJob(Long jobId) {
        Long orgId = TenantContext.requireOrganizationId();
        Job job = jobRepository.findByOrganizationIdAndId(orgId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BadRequestException("Job is already CLOSED");
        }

        job.setStatus(JobStatus.CLOSED);
        Job closed = jobRepository.save(job);

        auditLogService.logAction("HR", "hr@company.com", "CLOSE_JOB", "Job", closed.getId().toString(),
                getCurrentClientIp(), "Job closed with ID: " + closed.getId());

        return new JobResponse(closed, frontendUrl);
    }

    public JobResponse reopenJob(Long jobId) {
        Long orgId = TenantContext.requireOrganizationId();
        Job job = jobRepository.findByOrganizationIdAndId(orgId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (job.getStatus() != JobStatus.CLOSED) {
            throw new BadRequestException("Only CLOSED jobs can be reopened");
        }

        if (job.getApplicationDeadline() != null && job.getApplicationDeadline().isBefore(LocalDate.now())) {
            job.setStatus(JobStatus.DRAFT);
        } else {
            job.setStatus(JobStatus.PUBLISHED);
            job.setPublishedAt(LocalDateTime.now());
        }

        Job reopened = jobRepository.save(job);

        auditLogService.logAction("HR", "hr@company.com", "REOPEN_JOB", "Job", reopened.getId().toString(),
                getCurrentClientIp(), "Job reopened with ID: " + reopened.getId() + ", status set to: " + reopened.getStatus());

        return new JobResponse(reopened, frontendUrl);
    }

    public void deleteJob(Long jobId) {
        Long orgId = TenantContext.requireOrganizationId();
        Job job = jobRepository.findByOrganizationIdAndId(orgId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        if (applicationRepository.existsByOrganizationIdAndJobId(orgId, jobId)) {
            throw new BadRequestException("Cannot delete job with existing candidate applications. Consider closing the job instead.");
        }

        jobRepository.delete(job);

        auditLogService.logAction("HR", "hr@company.com", "DELETE_JOB", "Job", jobId.toString(),
                getCurrentClientIp(), "Deleted job ID: " + jobId);
    }

    public JobResponse duplicateJob(Long jobId) {
        Long orgId = TenantContext.requireOrganizationId();
        Job original = jobRepository.findByOrganizationIdAndId(orgId, jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

        Job duplicate = new Job();
        duplicate.setOrganizationId(orgId);
        duplicate.setDepartmentId(original.getDepartmentId());
        duplicate.setDepartment(original.getDepartment());
        duplicate.setTitle("Copy of " + original.getTitle());
        duplicate.setLocation(original.getLocation());
        duplicate.setEmploymentType(original.getEmploymentType());
        duplicate.setExperienceMin(original.getExperienceMin());
        duplicate.setExperienceMax(original.getExperienceMax());
        duplicate.setOpenings(original.getOpenings());
        duplicate.setSalaryMin(original.getSalaryMin());
        duplicate.setSalaryMax(original.getSalaryMax());
        duplicate.setSalaryRange(original.getSalaryRange());
        duplicate.setDescription(original.getDescription());
        duplicate.setRequirements(original.getRequirements());
        duplicate.setStatus(JobStatus.DRAFT);

        Job saved = jobRepository.save(duplicate);
        saved.setSlug(generateSlug(saved.getTitle(), saved.getId()));
        saved = jobRepository.save(saved);

        auditLogService.logAction("HR", "hr@company.com", "DUPLICATE_JOB", "Job", saved.getId().toString(),
                getCurrentClientIp(), "Duplicated job ID " + jobId + " to new job ID " + saved.getId());

        return new JobResponse(saved, frontendUrl);
    }

    private void validateJobForPublishing(Job job) {
        List<String> missingFields = new ArrayList<>();
        if (job.getTitle() == null || job.getTitle().isBlank()) missingFields.add("title");
        if (job.getDepartment() == null || job.getDepartment().isBlank()) missingFields.add("department");
        if (job.getLocation() == null || job.getLocation().isBlank()) missingFields.add("location");
        if (job.getEmploymentType() == null) missingFields.add("employment type");
        if (job.getDescription() == null || job.getDescription().isBlank()) missingFields.add("description");
        if (job.getRequirements() == null || job.getRequirements().isBlank()) missingFields.add("requirements");
        if (job.getOpenings() == null || job.getOpenings() <= 0) missingFields.add("openings");
        if (job.getApplicationDeadline() == null) missingFields.add("application deadline");

        if (!missingFields.isEmpty()) {
            throw new BadRequestException("JOB_NOT_READY_TO_PUBLISH: Missing fields: " + String.join(", ", missingFields));
        }
    }

    private String generateSlug(String title, Long jobId) {
        String titleSlug = (title != null ? title : "job")
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        return titleSlug + "-" + jobId;
    }

    private String getCurrentClientIp() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs =
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return com.example.ems.common.util.ClientIpResolver.getClientIp(attrs.getRequest());
            }
        } catch (Exception ignored) {}
        return "0.0.0.0";
    }
}
