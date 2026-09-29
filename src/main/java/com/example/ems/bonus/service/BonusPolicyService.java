package com.example.ems.bonus.service;

import com.example.ems.bonus.dto.BonusPolicyRequest;
import com.example.ems.bonus.dto.BonusPolicyResponse;
import com.example.ems.bonus.entity.BonusCalculationMethod;
import com.example.ems.bonus.entity.BonusPolicy;
import com.example.ems.bonus.entity.BonusPolicyStatus;
import com.example.ems.bonus.repository.BonusPolicyRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.example.ems.common.exception.ModuleDisabledException;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BonusPolicyService {

    private static final Logger log = LoggerFactory.getLogger(BonusPolicyService.class);

    private final BonusPolicyRepository policyRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public BonusPolicyService(BonusPolicyRepository policyRepository,
                              OrganizationRepository organizationRepository,
                              @Autowired(required = false) OrganizationCompensationConfigService compensationConfigService) {
        this.policyRepository = policyRepository;
        this.organizationRepository = organizationRepository;
        this.compensationConfigService = compensationConfigService;
    }

    public BonusPolicyResponse createPolicy(BonusPolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null && !compensationConfigService.isBonusEnabled(orgId)) {
            throw new ModuleDisabledException("BONUS_MODULE_DISABLED", "Bonus module is not enabled for this organization");
        }
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        if (policyRepository.existsByOrganizationIdAndNameIgnoreCase(orgId, request.getName().trim())) {
            throw new ConflictException("A bonus policy with name '" + request.getName() + "' already exists in this organization.");
        }

        validatePolicyDates(request.getEffectiveFrom(), request.getEffectiveTo());
        validateCalculationConfiguration(request);

        BonusPolicy policy = new BonusPolicy();
        policy.setOrganization(organization);
        policy.setName(request.getName().trim());
        policy.setDescription(request.getDescription());
        policy.setBonusType(request.getBonusType());
        policy.setCalculationMethod(request.getCalculationMethod());
        policy.setStatus(BonusPolicyStatus.DRAFT);
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setEffectiveTo(request.getEffectiveTo());
        policy.setFixedAmount(request.getFixedAmount());
        policy.setPercentage(request.getPercentage());
        policy.setMinimumAmount(request.getMinimumAmount());
        policy.setMaximumAmount(request.getMaximumAmount());
        policy.setTargetValue(request.getTargetValue());
        policy.setMinimumRating(request.getMinimumRating());
        policy.setRatingSlabsJson(request.getRatingSlabsJson());
        policy.setFormulaExpression(request.getFormulaExpression());
        policy.setDepartmentId(request.getDepartmentId());
        policy.setDesignationId(request.getDesignationId());
        policy.setEmployeeType(request.getEmployeeType());
        policy.setBranchId(request.getBranchId());
        policy.setPaymentFrequency(request.getPaymentFrequency());
        policy.setApprovalRequired(request.getApprovalRequired() != null ? request.getApprovalRequired() : true);
        policy.setPolicyVersion(1L);

        BonusPolicy saved = policyRepository.save(policy);
        log.info("Created DRAFT BonusPolicy ID={} for Org ID={}", saved.getId(), orgId);
        return BonusPolicyResponse.fromEntity(saved);
    }

    public BonusPolicyResponse updatePolicy(Long id, BonusPolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusPolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus policy not found with ID: " + id));

        if (policy.getStatus() != BonusPolicyStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT bonus policies can be edited. Active policies are immutable to preserve calculation integrity.");
        }

        if (policyRepository.existsByOrganizationIdAndNameIgnoreCaseAndIdNot(orgId, request.getName().trim(), id)) {
            throw new ConflictException("Another bonus policy with name '" + request.getName() + "' already exists.");
        }

        validatePolicyDates(request.getEffectiveFrom(), request.getEffectiveTo());
        validateCalculationConfiguration(request);

        policy.setName(request.getName().trim());
        policy.setDescription(request.getDescription());
        policy.setBonusType(request.getBonusType());
        policy.setCalculationMethod(request.getCalculationMethod());
        policy.setEffectiveFrom(request.getEffectiveFrom());
        policy.setEffectiveTo(request.getEffectiveTo());
        policy.setFixedAmount(request.getFixedAmount());
        policy.setPercentage(request.getPercentage());
        policy.setMinimumAmount(request.getMinimumAmount());
        policy.setMaximumAmount(request.getMaximumAmount());
        policy.setTargetValue(request.getTargetValue());
        policy.setMinimumRating(request.getMinimumRating());
        policy.setRatingSlabsJson(request.getRatingSlabsJson());
        policy.setFormulaExpression(request.getFormulaExpression());
        policy.setDepartmentId(request.getDepartmentId());
        policy.setDesignationId(request.getDesignationId());
        policy.setEmployeeType(request.getEmployeeType());
        policy.setBranchId(request.getBranchId());
        policy.setPaymentFrequency(request.getPaymentFrequency());
        policy.setApprovalRequired(request.getApprovalRequired() != null ? request.getApprovalRequired() : true);

        BonusPolicy saved = policyRepository.save(policy);
        log.info("Updated BonusPolicy ID={} in DRAFT status", saved.getId());
        return BonusPolicyResponse.fromEntity(saved);
    }

    public BonusPolicyResponse activatePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null && !compensationConfigService.isBonusEnabled(orgId)) {
            throw new ModuleDisabledException("BONUS_MODULE_DISABLED", "Bonus module is not enabled for this organization");
        }
        BonusPolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus policy not found with ID: " + id));

        if (policy.getStatus() == BonusPolicyStatus.ACTIVE) {
            throw new BadRequestException("Bonus policy is already ACTIVE.");
        }

        if (policy.getStatus() == BonusPolicyStatus.ARCHIVED) {
            throw new BadRequestException("ARCHIVED bonus policies cannot be activated.");
        }

        validatePolicyDates(policy.getEffectiveFrom(), policy.getEffectiveTo());
        validateCalculationConfiguration(policy);

        policy.setStatus(BonusPolicyStatus.ACTIVE);
        policy.setPolicyVersion(policy.getPolicyVersion() + 1L);

        BonusPolicy saved = policyRepository.save(policy);
        log.info("Activated BonusPolicy ID={} (new version={})", saved.getId(), saved.getPolicyVersion());
        return BonusPolicyResponse.fromEntity(saved);
    }

    public BonusPolicyResponse deactivatePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusPolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus policy not found with ID: " + id));

        if (policy.getStatus() == BonusPolicyStatus.INACTIVE) {
            return BonusPolicyResponse.fromEntity(policy);
        }

        policy.setStatus(BonusPolicyStatus.INACTIVE);
        BonusPolicy saved = policyRepository.save(policy);
        log.info("Deactivated BonusPolicy ID={}", saved.getId());
        return BonusPolicyResponse.fromEntity(saved);
    }

    public BonusPolicyResponse archivePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusPolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus policy not found with ID: " + id));

        policy.setStatus(BonusPolicyStatus.ARCHIVED);
        BonusPolicy saved = policyRepository.save(policy);
        log.info("Archived BonusPolicy ID={}", saved.getId());
        return BonusPolicyResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BonusPolicyResponse getPolicyById(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        BonusPolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Bonus policy not found with ID: " + id));
        return BonusPolicyResponse.fromEntity(policy);
    }

    @Transactional(readOnly = true)
    public Page<BonusPolicyResponse> getAllPolicies(BonusPolicyStatus status, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        Page<BonusPolicy> page = (status != null)
                ? policyRepository.findByOrganizationIdAndStatus(orgId, status, pageable)
                : policyRepository.findByOrganizationId(orgId, pageable);
        return page.map(BonusPolicyResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Optional<BonusPolicy> resolveApplicablePolicy(Employee employee, LocalDate evaluationDate) {
        Long orgId = TenantContext.requireOrganizationId();
        List<BonusPolicy> activePolicies = policyRepository.findActivePoliciesForDate(orgId, evaluationDate);

        return activePolicies.stream()
                .filter(p -> matchesScope(p, employee))
                .findFirst();
    }

    private boolean matchesScope(BonusPolicy p, Employee e) {
        if (p.getDepartmentId() != null && !p.getDepartmentId().isBlank()) {
            if (e.getDepartment() == null || !p.getDepartmentId().equalsIgnoreCase(e.getDepartment())) {
                return false;
            }
        }
        if (p.getDesignationId() != null && !p.getDesignationId().isBlank()) {
            if (e.getDesignation() == null || !p.getDesignationId().equalsIgnoreCase(e.getDesignation())) {
                return false;
            }
        }
        if (p.getBranchId() != null && !p.getBranchId().isBlank()) {
            if (e.getLocation() == null || !p.getBranchId().equalsIgnoreCase(e.getLocation())) {
                return false;
            }
        }
        if (p.getEmployeeType() != null && !p.getEmployeeType().isBlank()) {
            if (e.getEmploymentType() == null || !p.getEmployeeType().equalsIgnoreCase(e.getEmploymentType())) {
                return false;
            }
        }
        return true;
    }

    private void validatePolicyDates(LocalDate from, LocalDate to) {
        if (from == null) {
            throw new BadRequestException("Effective from date is mandatory.");
        }
        if (to != null && to.isBefore(from)) {
            throw new BadRequestException("Effective to date cannot be earlier than effective from date.");
        }
    }

    private void validateCalculationConfiguration(BonusPolicyRequest request) {
        if (request.getMinimumAmount() != null && request.getMaximumAmount() != null) {
            if (request.getMinimumAmount().compareTo(request.getMaximumAmount()) > 0) {
                throw new BadRequestException("Minimum amount cannot exceed maximum amount.");
            }
        }

        BonusCalculationMethod method = request.getCalculationMethod();
        if (method == BonusCalculationMethod.FIXED_AMOUNT) {
            if (request.getFixedAmount() == null || request.getFixedAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Fixed amount must be specified and non-negative for FIXED_AMOUNT method.");
            }
        } else if (method == BonusCalculationMethod.PERCENTAGE_OF_BASIC || method == BonusCalculationMethod.PERCENTAGE_OF_GROSS) {
            if (request.getPercentage() == null || request.getPercentage().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Percentage must be specified and non-negative for percentage-based methods.");
            }
        }
    }

    private void validateCalculationConfiguration(BonusPolicy policy) {
        if (policy.getMinimumAmount() != null && policy.getMaximumAmount() != null) {
            if (policy.getMinimumAmount().compareTo(policy.getMaximumAmount()) > 0) {
                throw new BadRequestException("Minimum amount cannot exceed maximum amount.");
            }
        }

        BonusCalculationMethod method = policy.getCalculationMethod();
        if (method == BonusCalculationMethod.FIXED_AMOUNT) {
            if (policy.getFixedAmount() == null || policy.getFixedAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Fixed amount must be configured for FIXED_AMOUNT method.");
            }
        } else if (method == BonusCalculationMethod.PERCENTAGE_OF_BASIC || method == BonusCalculationMethod.PERCENTAGE_OF_GROSS) {
            if (policy.getPercentage() == null || policy.getPercentage().compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("Percentage must be configured for percentage-based methods.");
            }
        }
    }
}
