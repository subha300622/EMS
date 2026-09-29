package com.example.ems.incentive.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.incentive.dto.IncentivePolicyRequest;
import com.example.ems.incentive.dto.IncentivePolicyResponse;
import com.example.ems.incentive.entity.IncentiveCalculationMethod;
import com.example.ems.incentive.entity.IncentivePolicy;
import com.example.ems.incentive.entity.IncentivePolicyStatus;
import com.example.ems.incentive.repository.IncentivePolicyRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IncentivePolicyService {

    private static final Logger log = LoggerFactory.getLogger(IncentivePolicyService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final IncentivePolicyRepository policyRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public IncentivePolicyService(IncentivePolicyRepository policyRepository,
                                  OrganizationRepository organizationRepository,
                                  @Autowired(required = false) OrganizationCompensationConfigService compensationConfigService) {
        this.policyRepository = policyRepository;
        this.organizationRepository = organizationRepository;
        this.compensationConfigService = compensationConfigService;
    }

    public IncentivePolicyResponse createPolicy(IncentivePolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireIncentiveEnabled(orgId);
        }
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        validatePolicyRequest(request);

        String policyName = request.getName() != null ? request.getName().trim() : request.getPolicyCode().trim();
        if (policyRepository.existsByOrganizationIdAndNameIgnoreCase(orgId, policyName)) {
            throw new ConflictException("An incentive policy with the name '" + policyName + "' already exists in this organization.");
        }

        IncentivePolicy policy = new IncentivePolicy();
        policy.setOrganization(organization);
        policy.setStatus(IncentivePolicyStatus.DRAFT);
        policy.setPolicyVersion(1L);

        mapDtoToEntity(request, policy);

        IncentivePolicy saved = policyRepository.save(policy);
        log.info("Created IncentivePolicy ID={} in DRAFT status for Org ID={}", saved.getId(), orgId);
        return IncentivePolicyResponse.fromEntity(saved);
    }

    public IncentivePolicyResponse updatePolicy(Long id, IncentivePolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireIncentiveEnabled(orgId);
        }
        IncentivePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive policy not found with ID: " + id));

        if (policy.getStatus() == IncentivePolicyStatus.ARCHIVED || policy.getStatus() == IncentivePolicyStatus.INACTIVE) {
            throw new BadRequestException("Cannot update an incentive policy in " + policy.getStatus() + " status.");
        }

        validatePolicyRequest(request);

        String policyName = request.getName() != null ? request.getName().trim() : request.getPolicyCode().trim();
        if (policyRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(orgId, policyName, id)) {
            throw new ConflictException("Another incentive policy with the name '" + policyName + "' already exists in this organization.");
        }

        mapDtoToEntity(request, policy);
        policy.setPolicyVersion(policy.getPolicyVersion() + 1);

        IncentivePolicy saved = policyRepository.save(policy);
        log.info("Updated IncentivePolicy ID={} (new version={}) for Org ID={}", saved.getId(), saved.getPolicyVersion(), orgId);
        return IncentivePolicyResponse.fromEntity(saved);
    }

    public IncentivePolicyResponse activatePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireIncentiveEnabled(orgId);
        }
        IncentivePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive policy not found with ID: " + id));

        if (policy.getStatus() != IncentivePolicyStatus.DRAFT && policy.getStatus() != IncentivePolicyStatus.INACTIVE) {
            throw new BadRequestException("Only DRAFT or INACTIVE policies can be transitioned to ACTIVE.");
        }

        policy.setStatus(IncentivePolicyStatus.ACTIVE);
        IncentivePolicy saved = policyRepository.save(policy);
        log.info("Activated IncentivePolicy ID={} for Org ID={}", saved.getId(), orgId);
        return IncentivePolicyResponse.fromEntity(saved);
    }

    public IncentivePolicyResponse deactivatePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        IncentivePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive policy not found with ID: " + id));

        if (policy.getStatus() != IncentivePolicyStatus.ACTIVE) {
            throw new BadRequestException("Only ACTIVE policies can be transitioned to INACTIVE.");
        }

        policy.setStatus(IncentivePolicyStatus.INACTIVE);
        IncentivePolicy saved = policyRepository.save(policy);
        log.info("Deactivated IncentivePolicy ID={} for Org ID={}", saved.getId(), orgId);
        return IncentivePolicyResponse.fromEntity(saved);
    }

    public IncentivePolicyResponse archivePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        IncentivePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive policy not found with ID: " + id));

        if (policy.getStatus() == IncentivePolicyStatus.ARCHIVED) {
            throw new BadRequestException("Incentive policy is already ARCHIVED.");
        }

        policy.setStatus(IncentivePolicyStatus.ARCHIVED);
        IncentivePolicy saved = policyRepository.save(policy);
        log.info("Archived IncentivePolicy ID={} for Org ID={}", saved.getId(), orgId);
        return IncentivePolicyResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public IncentivePolicyResponse getPolicyById(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        IncentivePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Incentive policy not found with ID: " + id));
        return IncentivePolicyResponse.fromEntity(policy);
    }

    @Transactional(readOnly = true)
    public Page<IncentivePolicyResponse> getAllPolicies(IncentivePolicyStatus status, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        Page<IncentivePolicy> page = (status != null)
                ? policyRepository.findByOrganizationIdAndStatus(orgId, status, pageable)
                : policyRepository.findByOrganizationId(orgId, pageable);
        return page.map(IncentivePolicyResponse::fromEntity);
    }

    /**
     * Resolves the most specific active applicable incentive policy for an employee on a given date.
     * Precedence: Department (+8), Designation (+4), EmployeeType (+2), Branch (+1), Fallback (0).
     */
    @Transactional(readOnly = true)
    public Optional<IncentivePolicy> resolveApplicablePolicy(Employee employee, LocalDate workDate) {
        Long orgId = employee.getOrganization() != null ? employee.getOrganization().getId() : TenantContext.requireOrganizationId();
        List<IncentivePolicy> candidatePolicies = policyRepository.findActivePoliciesForDate(orgId, workDate);

        return candidatePolicies.stream()
                .filter(p -> isPolicyApplicableToEmployee(p, employee))
                .max(Comparator.comparingInt(p -> calculateMatchScore(p, employee)));
    }

    private boolean isPolicyApplicableToEmployee(IncentivePolicy policy, Employee employee) {
        String empDept = employee != null ? employee.getDepartment() : null;
        String empDesig = employee != null ? employee.getDesignation() : null;
        String empType = employee != null ? employee.getEmploymentType() : null;
        String empLoc = employee != null ? employee.getLocation() : null;

        if (policy.getDepartmentId() != null && !policy.getDepartmentId().isBlank()) {
            if (empDept == null || !policy.getDepartmentId().equalsIgnoreCase(empDept)) {
                return false;
            }
        }
        if (policy.getDesignationId() != null && !policy.getDesignationId().isBlank()) {
            if (empDesig == null || !policy.getDesignationId().equalsIgnoreCase(empDesig)) {
                return false;
            }
        }
        if (policy.getEmployeeType() != null && !policy.getEmployeeType().isBlank()) {
            if (empType == null || !policy.getEmployeeType().equalsIgnoreCase(empType)) {
                return false;
            }
        }
        if (policy.getBranchId() != null && !policy.getBranchId().isBlank()) {
            if (empLoc == null || !policy.getBranchId().equalsIgnoreCase(empLoc)) {
                return false;
            }
        }
        return true;
    }

    private int calculateMatchScore(IncentivePolicy policy, Employee employee) {
        int score = 0;
        if (policy.getDepartmentId() != null && !policy.getDepartmentId().isBlank()) score += 8;
        if (policy.getDesignationId() != null && !policy.getDesignationId().isBlank()) score += 4;
        if (policy.getEmployeeType() != null && !policy.getEmployeeType().isBlank()) score += 2;
        if (policy.getBranchId() != null && !policy.getBranchId().isBlank()) score += 1;
        return score;
    }

    private void validatePolicyRequest(IncentivePolicyRequest req) {
        if (req.getEffectiveTo() != null && req.getEffectiveTo().isBefore(req.getEffectiveFrom())) {
            throw new BadRequestException("effectiveTo date cannot be before effectiveFrom date.");
        }

        if (req.getCalculationMethod() == IncentiveCalculationMethod.FIXED_AMOUNT) {
            if (req.getFixedAmount() == null || req.getFixedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("fixedAmount is mandatory and must be greater than 0 for FIXED_AMOUNT calculation.");
            }
        } else if (req.getCalculationMethod() == IncentiveCalculationMethod.PERCENTAGE_OF_BASIC ||
                   req.getCalculationMethod() == IncentiveCalculationMethod.PERCENTAGE_OF_GROSS ||
                   req.getCalculationMethod() == IncentiveCalculationMethod.PERCENTAGE_OF_ACHIEVEMENT) {
            if (req.getPercentage() == null || req.getPercentage().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("percentage is mandatory and must be greater than 0 for percentage-based calculation.");
            }
        } else if (req.getCalculationMethod() == IncentiveCalculationMethod.TARGET_SLAB) {
            if (req.getTargetSlabs() == null || req.getTargetSlabs().isEmpty()) {
                throw new BadRequestException("targetSlabs must not be empty for TARGET_SLAB calculation method.");
            }
        }

        if (req.getMinimumAmount() != null && req.getMaximumAmount() != null) {
            if (req.getMinimumAmount().compareTo(req.getMaximumAmount()) > 0) {
                throw new BadRequestException("minimumAmount cannot be greater than maximumAmount.");
            }
        }
    }

    private void mapDtoToEntity(IncentivePolicyRequest req, IncentivePolicy policy) {
        String name = req.getName() != null ? req.getName().trim() : (req.getPolicyCode() != null ? req.getPolicyCode().trim() : null);
        policy.setName(name);
        policy.setPolicyCode(req.getPolicyCode() != null ? req.getPolicyCode().trim() : name);
        policy.setDescription(req.getDescription());
        policy.setIncentiveType(req.getIncentiveType());
        policy.setCalculationMethod(req.getCalculationMethod());
        policy.setEffectiveFrom(req.getEffectiveFrom());
        policy.setEffectiveTo(req.getEffectiveTo());
        policy.setFixedAmount(req.getFixedAmount());
        policy.setPercentage(req.getPercentage());
        policy.setMinimumAmount(req.getMinimumAmount());
        policy.setMaximumAmount(req.getMaximumAmount());
        policy.setTargetValue(req.getTargetValue());
        policy.setMinimumAchievementPercentage(req.getMinimumAchievementPercentage());
        policy.setMinimumRating(req.getMinimumRating());
        policy.setFormulaExpression(req.getFormulaExpression());

        if (req.getTargetSlabs() != null && !req.getTargetSlabs().isEmpty()) {
            try {
                policy.setTargetSlabsJson(MAPPER.writeValueAsString(req.getTargetSlabs()));
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Failed to serialize target slabs: " + e.getMessage());
            }
        } else {
            policy.setTargetSlabsJson(null);
        }

        policy.setDepartmentId(req.getDepartmentId());
        policy.setDesignationId(req.getDesignationId());
        policy.setEmployeeType(req.getEmployeeType());
        policy.setBranchId(req.getBranchId());

        if (req.getPaymentFrequency() != null) {
            policy.setPaymentFrequency(req.getPaymentFrequency());
        }
        if (req.getApprovalRequired() != null) {
            policy.setApprovalRequired(req.getApprovalRequired());
        }
    }
}
