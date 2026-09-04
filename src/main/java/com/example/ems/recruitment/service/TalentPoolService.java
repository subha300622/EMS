package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.recruitment.dto.CandidateCreateRequest;
import com.example.ems.recruitment.dto.TalentPoolCandidateResponse;
import com.example.ems.recruitment.dto.TalentPoolInviteRequest;
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

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class TalentPoolService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TalentPoolInvitationRepository invitationRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<TalentPoolCandidateResponse> searchTalentPool(
            String skill, Double experienceMin, Double experienceMax, String location, String search, Pageable pageable) {

        Long orgId = TenantContext.requireOrganizationId();

        Specification<Candidate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organizationId"), orgId));
            predicates.add(cb.notEqual(root.get("talentPoolStatus"), TalentPoolStatus.HIRED));

            if (experienceMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experienceYears"), experienceMin));
            }
            if (experienceMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceYears"), experienceMax));
            }
            if (skill != null && !skill.isBlank()) {
                String skillLike = "%" + skill.toLowerCase() + "%";
                Predicate coverLetterMatch = cb.like(cb.lower(root.get("coverLetter")), skillLike);
                Predicate designationMatch = cb.like(cb.lower(root.get("currentDesignation")), skillLike);
                predicates.add(cb.or(coverLetterMatch, designationMatch));
            }
            if (search != null && !search.isBlank()) {
                String searchLike = "%" + search.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName")), searchLike);
                Predicate emailMatch = cb.like(cb.lower(root.get("email")), searchLike);
                Predicate phoneMatch = cb.like(cb.lower(root.get("phone")), searchLike);
                predicates.add(cb.or(nameMatch, emailMatch, phoneMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return candidateRepository.findAll(spec, pageable).map(TalentPoolCandidateResponse::new);
    }

    public TalentPoolCandidateResponse addCandidate(CandidateCreateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        candidateRepository.findByOrganizationIdAndEmail(orgId, request.getEmail())
                .ifPresent(existing -> {
                    throw new ConflictException("Candidate with email " + request.getEmail() + " already exists in your organization");
                });

        Candidate candidate = new Candidate();
        candidate.setOrganizationId(orgId);
        candidate.setFullName(request.getFullName());
        candidate.setEmail(request.getEmail());
        candidate.setPhone(request.getPhone());
        candidate.setExperienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0.0);
        candidate.setCurrentCompany(request.getCurrentCompany());
        candidate.setCurrentDesignation(request.getCurrentDesignation());
        candidate.setExpectedSalary(request.getExpectedSalary());
        candidate.setResumeUrl(request.getResumeUrl());
        candidate.setCoverLetter(request.getCoverLetter());
        candidate.setTalentPoolStatus(TalentPoolStatus.AVAILABLE);

        Candidate saved = candidateRepository.save(candidate);

        auditLogService.logAction("HR", "hr@company.com", "ADD_TALENT_POOL_CANDIDATE", "Candidate",
                saved.getId().toString(), getCurrentClientIp(), "Manually added candidate " + saved.getFullName() + " to talent pool");

        return new TalentPoolCandidateResponse(saved);
    }

    public void deleteCandidate(Long candidateId) {
        Long orgId = TenantContext.requireOrganizationId();
        Candidate candidate = candidateRepository.findByOrganizationIdAndId(orgId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + candidateId));

        if (!applicationRepository.findByOrganizationIdAndCandidateId(orgId, candidateId).isEmpty()) {
            throw new BadRequestException("Cannot delete candidate with active application records");
        }

        candidateRepository.delete(candidate);

        auditLogService.logAction("HR", "hr@company.com", "DELETE_TALENT_POOL_CANDIDATE", "Candidate",
                candidateId.toString(), getCurrentClientIp(), "Deleted candidate ID: " + candidateId + " from talent pool");
    }

    public void inviteCandidate(Long candidateId, TalentPoolInviteRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        Candidate candidate = candidateRepository.findByOrganizationIdAndId(orgId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + candidateId));

        Job job = jobRepository.findByOrganizationIdAndId(orgId, request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + request.getJobId()));

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new BadRequestException("Invitations can only be sent for PUBLISHED jobs");
        }

        boolean alreadyApplied = applicationRepository.existsByOrganizationIdAndCandidateIdAndJobId(
                orgId, candidateId, request.getJobId());

        if (alreadyApplied) {
            throw new ConflictException("Candidate has already applied for this job");
        }

        boolean alreadyInvited = invitationRepository.existsByOrganizationIdAndCandidateIdAndJobId(
                orgId, candidateId, request.getJobId());

        if (alreadyInvited) {
            throw new ConflictException("Invitation has already been sent to candidate for this job");
        }

        TalentPoolInvitation invitation = new TalentPoolInvitation();
        invitation.setOrganizationId(orgId);
        invitation.setCandidate(candidate);
        invitation.setJob(job);
        invitation.setInvitedBy("HR");
        invitation.setStatus("INVITED");

        invitationRepository.save(invitation);

        auditLogService.logAction("HR", "hr@company.com", "INVITE_TALENT_POOL_CANDIDATE", "Candidate",
                candidate.getId().toString(), getCurrentClientIp(), "Invited candidate " + candidate.getFullName() + " for job " + job.getTitle());
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
