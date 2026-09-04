package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
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
                candidate.getId().toString(), "127.0.0.1", "Invited candidate " + candidate.getFullName() + " for job " + job.getTitle());
    }
}
