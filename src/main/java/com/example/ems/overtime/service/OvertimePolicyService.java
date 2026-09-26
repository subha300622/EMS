package com.example.ems.overtime.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import com.example.ems.overtime.dto.OvertimePolicyRequest;
import com.example.ems.overtime.dto.OvertimePolicyResponse;
import com.example.ems.overtime.entity.OvertimeAmountBasis;
import com.example.ems.overtime.entity.OvertimePolicy;
import com.example.ems.overtime.entity.OvertimePolicyStatus;
import com.example.ems.overtime.repository.OvertimePolicyRepository;
import com.example.ems.security.context.TenantContext;
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

@Service
@Transactional
public class OvertimePolicyService {

    private static final Logger log = LoggerFactory.getLogger(OvertimePolicyService.class);

    private final OvertimePolicyRepository policyRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public OvertimePolicyService(OvertimePolicyRepository policyRepository,
                                 OrganizationRepository organizationRepository,
                                 @Autowired(required = false) OrganizationCompensationConfigService compensationConfigService) {
        this.policyRepository = policyRepository;
        this.organizationRepository = organizationRepository;
        this.compensationConfigService = compensationConfigService;
    }

    public OvertimePolicyService(OvertimePolicyRepository policyRepository,
                                 OrganizationRepository organizationRepository) {
        this(policyRepository, organizationRepository, null);
    }

    public OvertimePolicyResponse createPolicy(OvertimePolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireOvertimeEnabled(orgId);
        }
        validatePolicyRequest(request);

        if (policyRepository.existsByOrganizationIdAndName(orgId, request.getName().trim())) {
            throw new ConflictException("An overtime policy with name '" + request.getName() + "' already exists.");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + orgId));

        OvertimePolicy policy = new OvertimePolicy();
        policy.setOrganization(org);
        mapRequestToEntity(request, policy);
        policy.setStatus(OvertimePolicyStatus.DRAFT);

        policy = policyRepository.save(policy);
        log.info("Created OvertimePolicy ID={} for Org ID={}", policy.getId(), orgId);
        return OvertimePolicyResponse.fromEntity(policy);
    }

    public OvertimePolicyResponse updatePolicy(Long id, OvertimePolicyRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireOvertimeEnabled(orgId);
        }
        validatePolicyRequest(request);

        OvertimePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime policy not found with ID: " + id));

        if (policy.getStatus() == OvertimePolicyStatus.ARCHIVED) {
            throw new BadRequestException("Cannot update an ARCHIVED overtime policy.");
        }

        if (policyRepository.existsByOrganizationIdAndNameAndIdNot(orgId, request.getName().trim(), id)) {
            throw new ConflictException("Another overtime policy already exists with name '" + request.getName() + "'.");
        }

        mapRequestToEntity(request, policy);
        policy = policyRepository.save(policy);
        log.info("Updated OvertimePolicy ID={} for Org ID={}", policy.getId(), orgId);
        return OvertimePolicyResponse.fromEntity(policy);
    }

    public OvertimePolicyResponse activatePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireOvertimeEnabled(orgId);
        }
        OvertimePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime policy not found with ID: " + id));

        if (policy.getStatus() == OvertimePolicyStatus.ARCHIVED) {
            throw new BadRequestException("Cannot activate an ARCHIVED overtime policy.");
        }

        policy.setStatus(OvertimePolicyStatus.ACTIVE);
        policy = policyRepository.save(policy);
        log.info("Activated OvertimePolicy ID={} for Org ID={}", policy.getId(), orgId);
        return OvertimePolicyResponse.fromEntity(policy);
    }

    public OvertimePolicyResponse deactivatePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        OvertimePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime policy not found with ID: " + id));

        if (policy.getStatus() == OvertimePolicyStatus.ARCHIVED) {
            throw new BadRequestException("Cannot deactivate an ARCHIVED overtime policy.");
        }

        policy.setStatus(OvertimePolicyStatus.INACTIVE);
        policy = policyRepository.save(policy);
        log.info("Deactivated OvertimePolicy ID={} for Org ID={}", policy.getId(), orgId);
        return OvertimePolicyResponse.fromEntity(policy);
    }

    public OvertimePolicyResponse archivePolicy(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        OvertimePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime policy not found with ID: " + id));

        policy.setStatus(OvertimePolicyStatus.ARCHIVED);
        policy = policyRepository.save(policy);
        log.info("Archived OvertimePolicy ID={} for Org ID={}", policy.getId(), orgId);
        return OvertimePolicyResponse.fromEntity(policy);
    }

    @Transactional(readOnly = true)
    public OvertimePolicyResponse getPolicyById(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        OvertimePolicy policy = policyRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Overtime policy not found with ID: " + id));
        return OvertimePolicyResponse.fromEntity(policy);
    }

    @Transactional(readOnly = true)
    public Page<OvertimePolicyResponse> getAllPolicies(OvertimePolicyStatus status, Pageable pageable) {
        Long orgId = TenantContext.requireOrganizationId();
        Page<OvertimePolicy> page = (status != null)
                ? policyRepository.findByOrganizationIdAndStatus(orgId, status, pageable)
                : policyRepository.findByOrganizationId(orgId, pageable);
        return page.map(OvertimePolicyResponse::fromEntity);
    }

    /**
     * Resolves the best-matching ACTIVE OvertimePolicy for an employee on a target work date.
     * Precedence (Deterministic Best-Match):
     * 1. 4-attribute match (dept + desig + empType + branch)
     * 2. 3-attribute matches
     * 3. 2-attribute matches
     * 4. 1-attribute matches
     * 5. Org-wide fallback policy (all 4 attributes null)
     */
    @Transactional(readOnly = true)
    public OvertimePolicy resolveApplicablePolicy(Long organizationId, Employee employee, LocalDate workDate) {
        List<OvertimePolicy> activePolicies = policyRepository.findActivePoliciesForDate(organizationId, workDate);
        if (activePolicies.isEmpty()) {
            throw new ResourceNotFoundException("No active overtime policy found for organization on " + workDate);
        }

        String empDept = employee != null ? employee.getDepartment() : null;
        String empDesig = employee != null ? employee.getDesignation() : null;
        String empType = employee != null ? employee.getEmploymentType() : null;
        String empBranch = employee != null ? employee.getLocation() : null;

        OvertimePolicy bestMatch = null;
        int highestScore = -1;

        for (OvertimePolicy p : activePolicies) {
            int score = calculatePolicyMatchScore(p, empDept, empDesig, empType, empBranch);
            if (score > highestScore) {
                highestScore = score;
                bestMatch = p;
            }
        }

        if (bestMatch == null) {
            throw new ResourceNotFoundException("No applicable overtime policy matched for employee on " + workDate);
        }

        return bestMatch;
    }

    private int calculatePolicyMatchScore(OvertimePolicy policy, String empDept, String empDesig, String empType, String empBranch) {
        // If a policy defines a filter that mismatches the employee, score = -1 (disqualified)
        if (policy.getDepartmentId() != null && !policy.getDepartmentId().equalsIgnoreCase(empDept)) {
            return -1;
        }
        if (policy.getDesignationId() != null && !policy.getDesignationId().equalsIgnoreCase(empDesig)) {
            return -1;
        }
        if (policy.getEmployeeType() != null && !policy.getEmployeeType().equalsIgnoreCase(empType)) {
            return -1;
        }
        if (policy.getBranchId() != null && !policy.getBranchId().equalsIgnoreCase(empBranch)) {
            return -1;
        }

        // Weighted specificity score:
        int score = 0;
        if (policy.getDepartmentId() != null) score += 8;
        if (policy.getDesignationId() != null) score += 4;
        if (policy.getEmployeeType() != null) score += 2;
        if (policy.getBranchId() != null) score += 1;
        return score; // Org fallback (0 filters specified) has score = 0
    }

    private void validatePolicyRequest(OvertimePolicyRequest req) {
        if (req.getEffectiveTo() != null && req.getEffectiveTo().isBefore(req.getEffectiveFrom())) {
            throw new BadRequestException("effectiveTo (" + req.getEffectiveTo() + ") cannot be before effectiveFrom (" + req.getEffectiveFrom() + ").");
        }
        if (req.getMinimumOtMinutes() != null && req.getMaximumOtMinutes() != null
                && req.getMinimumOtMinutes() > req.getMaximumOtMinutes()) {
            throw new BadRequestException("minimumOtMinutes (" + req.getMinimumOtMinutes() + ") cannot exceed maximumOtMinutes (" + req.getMaximumOtMinutes() + ").");
        }
        if (req.getAmountBasis() == OvertimeAmountBasis.FIXED_HOURLY_RATE) {
            if (req.getFixedHourlyRate() == null || req.getFixedHourlyRate().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("fixedHourlyRate is mandatory and must be > 0 when amountBasis is FIXED_HOURLY_RATE.");
            }
        }
    }

    private void mapRequestToEntity(OvertimePolicyRequest req, OvertimePolicy entity) {
        entity.setName(req.getName().trim());
        entity.setDescription(req.getDescription());
        entity.setEffectiveFrom(req.getEffectiveFrom());
        entity.setEffectiveTo(req.getEffectiveTo());
        entity.setNormalWorkingHours(req.getNormalWorkingHours());
        entity.setMinimumOtMinutes(req.getMinimumOtMinutes());
        entity.setMaximumOtMinutes(req.getMaximumOtMinutes());
        entity.setAmountBasis(req.getAmountBasis());
        entity.setFixedHourlyRate(req.getFixedHourlyRate());
        entity.setWorkingDaysPerMonth(req.getWorkingDaysPerMonth());
        entity.setWorkingHoursPerDay(req.getWorkingHoursPerDay());
        entity.setNormalDayMultiplier(req.getNormalDayMultiplier());
        entity.setWeekendMultiplier(req.getWeekendMultiplier());
        entity.setHolidayMultiplier(req.getHolidayMultiplier());
        entity.setRoundingRule(req.getRoundingRule());
        entity.setApprovalRequired(req.getApprovalRequired());
        entity.setDepartmentId(req.getDepartmentId());
        entity.setDesignationId(req.getDesignationId());
        entity.setEmployeeType(req.getEmployeeType());
        entity.setBranchId(req.getBranchId());
    }
}
