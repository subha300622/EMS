package com.example.ems.increment.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.increment.dto.CreateIncrementPolicyRequest;
import com.example.ems.increment.dto.IncrementPolicyResponse;
import com.example.ems.increment.dto.PolicyBandDto;
import com.example.ems.increment.dto.UpdateIncrementPolicyRequest;
import com.example.ems.increment.entity.EffectiveDateRule;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.entity.IncrementPolicyBand;
import com.example.ems.increment.repository.IncrementPolicyRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;

@Service
public class IncrementPolicyService {

    @Autowired
    @Qualifier("enterpriseIncrementPolicyRepository")
    private IncrementPolicyRepository policyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Transactional
    public IncrementPolicyResponse createPolicy(CreateIncrementPolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        validatePolicyRequest(request);

        Integer maxVersion = policyRepository.findMaxVersionByOrgId(orgId);
        int nextVersion = (maxVersion != null) ? maxVersion + 1 : 1;

        if (Boolean.TRUE.equals(request.getActive())) {
            // Deactivate previous active policies to enforce single active policy rule
            List<IncrementPolicy> activePolicies = policyRepository.findActivePoliciesByOrgId(orgId);
            for (IncrementPolicy p : activePolicies) {
                p.setActive(false);
                policyRepository.save(p);
            }
        }

        IncrementPolicy policy = new IncrementPolicy();
        policy.setOrganization(organization);
        policy.setName(request.getName().trim());
        policy.setVersion(nextVersion);
        policy.setAppraisalRequired(Boolean.TRUE.equals(request.getAppraisalRequired()));
        policy.setMinimumRating(request.getMinimumRating() != null ? request.getMinimumRating() : 3.0);
        policy.setMinimumGoalAchievementPercentage(request.getMinimumGoalAchievementPercentage() != null ? request.getMinimumGoalAchievementPercentage() : 70.0);
        policy.setMinimumAttendancePercentage(request.getMinimumAttendancePercentage() != null ? request.getMinimumAttendancePercentage() : 90.0);
        policy.setMinimumServiceMonths(request.getMinimumServiceMonths() != null ? request.getMinimumServiceMonths() : 12);
        policy.setMaximumIncrementPercentage(request.getMaximumIncrementPercentage() != null ? request.getMaximumIncrementPercentage() : 20.0);
        policy.setMinimumIncrementPercentage(request.getMinimumIncrementPercentage() != null ? request.getMinimumIncrementPercentage() : 0.0);
        policy.setEffectiveDateRule(request.getEffectiveDateRule() != null ? request.getEffectiveDateRule() : EffectiveDateRule.FIXED_DATE);
        policy.setEffectiveDate(request.getEffectiveDate());
        policy.setBudgetLimit(request.getBudgetLimit() != null ? request.getBudgetLimit() : BigDecimal.ZERO);
        policy.setActive(request.getActive() != null ? request.getActive() : true);

        if (request.getBands() != null) {
            for (PolicyBandDto bandDto : request.getBands()) {
                if (bandDto.getMinRating() != null && bandDto.getMaxRating() != null && bandDto.getIncrementPercentage() != null) {
                    policy.addBand(new IncrementPolicyBand(bandDto.getMinRating(), bandDto.getMaxRating(), bandDto.getIncrementPercentage()));
                }
            }
        }

        IncrementPolicy saved = policyRepository.save(policy);
        return mapToResponse(saved);
    }

    @Transactional
    public IncrementPolicyResponse updatePolicy(Long id, UpdateIncrementPolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementPolicy policy = policyRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment policy not found with ID: " + id));

        if (request.getName() != null && !request.getName().isBlank()) {
            policy.setName(request.getName().trim());
        }
        if (request.getAppraisalRequired() != null) {
            policy.setAppraisalRequired(request.getAppraisalRequired());
        }
        if (request.getMinimumRating() != null) {
            if (request.getMinimumRating() < 0) throw new BadRequestException("minimumRating must be >= 0");
            policy.setMinimumRating(request.getMinimumRating());
        }
        if (request.getMinimumGoalAchievementPercentage() != null) {
            if (request.getMinimumGoalAchievementPercentage() < 0 || request.getMinimumGoalAchievementPercentage() > 100) {
                throw new BadRequestException("minimumGoalAchievementPercentage must be between 0 and 100");
            }
            policy.setMinimumGoalAchievementPercentage(request.getMinimumGoalAchievementPercentage());
        }
        if (request.getMinimumAttendancePercentage() != null) {
            if (request.getMinimumAttendancePercentage() < 0 || request.getMinimumAttendancePercentage() > 100) {
                throw new BadRequestException("minimumAttendancePercentage must be between 0 and 100");
            }
            policy.setMinimumAttendancePercentage(request.getMinimumAttendancePercentage());
        }
        if (request.getMinimumServiceMonths() != null) {
            policy.setMinimumServiceMonths(request.getMinimumServiceMonths());
        }
        if (request.getMaximumIncrementPercentage() != null) {
            if (request.getMaximumIncrementPercentage() <= 0) throw new BadRequestException("maximumIncrementPercentage must be > 0");
            policy.setMaximumIncrementPercentage(request.getMaximumIncrementPercentage());
        }
        if (request.getMinimumIncrementPercentage() != null) {
            if (request.getMinimumIncrementPercentage() < 0 || request.getMinimumIncrementPercentage() > policy.getMaximumIncrementPercentage()) {
                throw new BadRequestException("minimumIncrementPercentage must be between 0 and maximumIncrementPercentage");
            }
            policy.setMinimumIncrementPercentage(request.getMinimumIncrementPercentage());
        }
        if (request.getEffectiveDateRule() != null) {
            policy.setEffectiveDateRule(request.getEffectiveDateRule());
        }
        if (request.getEffectiveDate() != null) {
            policy.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getBudgetLimit() != null) {
            if (request.getBudgetLimit().compareTo(BigDecimal.ZERO) < 0) throw new BadRequestException("budgetLimit must be >= 0");
            policy.setBudgetLimit(request.getBudgetLimit());
        }
        if (request.getActive() != null) {
            if (Boolean.TRUE.equals(request.getActive())) {
                List<IncrementPolicy> activePolicies = policyRepository.findActivePoliciesByOrgId(orgId);
                for (IncrementPolicy p : activePolicies) {
                    if (!p.getId().equals(id)) {
                        p.setActive(false);
                        policyRepository.save(p);
                    }
                }
            }
            policy.setActive(request.getActive());
        }

        if (request.getBands() != null) {
            policy.getBands().clear();
            for (PolicyBandDto bandDto : request.getBands()) {
                if (bandDto.getMinRating() != null && bandDto.getMaxRating() != null && bandDto.getIncrementPercentage() != null) {
                    policy.addBand(new IncrementPolicyBand(bandDto.getMinRating(), bandDto.getMaxRating(), bandDto.getIncrementPercentage()));
                }
            }
        }

        IncrementPolicy saved = policyRepository.save(policy);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncrementPolicyResponse getCurrentActivePolicy() {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementPolicy policy = policyRepository.findFirstActiveByOrgId(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("No active increment policy is configured."));
        return mapToResponse(policy);
    }

    @Transactional(readOnly = true)
    public IncrementPolicy getPolicyEntity(Long policyId) {
        Long orgId = TenantContext.requireOrganizationId();
        return policyRepository.findByIdAndOrgId(policyId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment policy not found with ID: " + policyId));
    }

    @Transactional(readOnly = true)
    public List<IncrementPolicyResponse> getAllPolicies() {
        Long orgId = TenantContext.requireOrganizationId();
        return policyRepository.findAllByOrgId(orgId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validatePolicyRequest(CreateIncrementPolicyRequest request) {
        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new BadRequestException("Policy name is required and cannot be blank.");
        }
        if (Boolean.TRUE.equals(request.getAppraisalRequired())) {
            if (request.getMinimumRating() != null && (request.getMinimumRating() < 1.0 || request.getMinimumRating() > 5.0)) {
                throw new BadRequestException("minimumRating must be between 1.0 and 5.0 when appraisal is required.");
            }
        } else if (request.getMinimumRating() != null && request.getMinimumRating() < 0) {
            throw new BadRequestException("minimumRating must be >= 0");
        }
        if (request.getMinimumGoalAchievementPercentage() != null &&
                (request.getMinimumGoalAchievementPercentage() < 0 || request.getMinimumGoalAchievementPercentage() > 100)) {
            throw new BadRequestException("minimumGoalAchievementPercentage must be between 0 and 100");
        }
        if (request.getMinimumAttendancePercentage() != null &&
                (request.getMinimumAttendancePercentage() < 0 || request.getMinimumAttendancePercentage() > 100)) {
            throw new BadRequestException("minimumAttendancePercentage must be between 0 and 100");
        }
        if (request.getMaximumIncrementPercentage() != null && request.getMaximumIncrementPercentage() <= 0) {
            throw new BadRequestException("maximumIncrementPercentage must be > 0");
        }
        if (request.getMinimumIncrementPercentage() != null &&
                (request.getMinimumIncrementPercentage() < 0 ||
                 (request.getMaximumIncrementPercentage() != null && request.getMinimumIncrementPercentage() > request.getMaximumIncrementPercentage()))) {
            throw new BadRequestException("minimumIncrementPercentage must be >= 0 and <= maximumIncrementPercentage");
        }
        if (request.getBudgetLimit() != null && request.getBudgetLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("budgetLimit must be >= 0");
        }
        if (request.getEffectiveDateRule() == EffectiveDateRule.FIXED_DATE && request.getEffectiveDate() == null) {
            throw new BadRequestException("effectiveDate is required when effectiveDateRule is FIXED_DATE");
        }
    }

    public IncrementPolicyResponse mapToResponse(IncrementPolicy policy) {
        IncrementPolicyResponse resp = new IncrementPolicyResponse();
        resp.setId(policy.getId());
        resp.setName(policy.getName());
        resp.setVersion(policy.getVersion());
        resp.setAppraisalRequired(policy.getAppraisalRequired());
        resp.setMinimumRating(policy.getMinimumRating());
        resp.setMinimumGoalAchievementPercentage(policy.getMinimumGoalAchievementPercentage());
        resp.setMinimumAttendancePercentage(policy.getMinimumAttendancePercentage());
        resp.setMinimumServiceMonths(policy.getMinimumServiceMonths());
        resp.setMaximumIncrementPercentage(policy.getMaximumIncrementPercentage());
        resp.setMinimumIncrementPercentage(policy.getMinimumIncrementPercentage());
        resp.setEffectiveDateRule(policy.getEffectiveDateRule());
        resp.setEffectiveDate(policy.getEffectiveDate());
        resp.setBudgetLimit(policy.getBudgetLimit());
        resp.setActive(policy.getActive());
        resp.setCreatedAt(policy.getCreatedAt());
        resp.setUpdatedAt(policy.getUpdatedAt());

        if (policy.getBands() != null) {
            List<PolicyBandDto> bandDtos = policy.getBands().stream()
                    .map(b -> new PolicyBandDto(b.getMinRating(), b.getMaxRating(), b.getIncrementPercentage()))
                    .collect(Collectors.toList());
            resp.setBands(bandDtos);
        } else {
            resp.setBands(new ArrayList<>());
        }
        return resp;
    }
}
