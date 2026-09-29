package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.*;
import com.example.ems.increment.eligibility.IncrementEligibilityService;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementCycleStatus;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.repository.IncrementCycleRepository;
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
public class IncrementCycleService {

    @Autowired
    private IncrementCycleRepository cycleRepository;

    @Autowired
    @Qualifier("enterpriseIncrementPolicyRepository")
    private IncrementPolicyRepository policyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private IncrementEligibilityService eligibilityService;

    @Transactional
    public IncrementCycleResponse createCycle(CreateIncrementCycleRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization organization = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (request.getName() == null || request.getName().trim().isBlank()) {
            throw new BadRequestException("Increment cycle name is required.");
        }
        if (request.getFinancialYear() == null || request.getFinancialYear().trim().isBlank()) {
            throw new BadRequestException("Financial year is required.");
        }
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BadRequestException("Start date and end date are required.");
        }
        if (!request.getStartDate().isBefore(request.getEndDate())) {
            throw new BadRequestException("Start date must be before end date.");
        }
        if (request.getEffectiveDate() == null || request.getEffectiveDate().isBefore(request.getEndDate())) {
            throw new BadRequestException("Effective date must be on or after end date.");
        }

        IncrementPolicy policy = policyRepository.findByIdAndOrgId(request.getPolicyId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment policy not found for this organization."));

        if (!Boolean.TRUE.equals(policy.getActive())) {
            throw new BadRequestException("Cannot associate an inactive increment policy with a cycle.");
        }

        List<IncrementCycle> overlapping = cycleRepository.findOverlappingActiveCycles(orgId, request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Cannot create overlapping active increment cycles for the organization.");
        }

        IncrementCycle cycle = new IncrementCycle();
        cycle.setOrganization(organization);
        cycle.setName(request.getName().trim());
        cycle.setFinancialYear(request.getFinancialYear().trim());
        cycle.setStartDate(request.getStartDate());
        cycle.setEndDate(request.getEndDate());
        cycle.setEffectiveDate(request.getEffectiveDate());
        cycle.setPolicy(policy);
        cycle.setBudgetLimit(request.getBudgetLimit() != null ? request.getBudgetLimit() : policy.getBudgetLimit());
        cycle.setAllocatedBudget(BigDecimal.ZERO);
        cycle.setStatus(IncrementCycleStatus.DRAFT);

        IncrementCycle saved = cycleRepository.save(cycle);
        return mapToResponse(saved);
    }

    @Transactional
    public IncrementCycleResponse openCycle(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementCycle cycle = cycleRepository.findByIdAndOrgId(cycleId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment cycle not found with ID: " + cycleId));

        if (cycle.getStatus() != IncrementCycleStatus.DRAFT) {
            throw new BadRequestException("Increment cycle cannot be opened from state: " + cycle.getStatus() + ". Only DRAFT cycles can be opened.");
        }

        if (!Boolean.TRUE.equals(cycle.getPolicy().getActive())) {
            throw new BadRequestException("Associated increment policy is not active.");
        }

        cycle.setStatus(IncrementCycleStatus.OPEN);
        IncrementCycle saved = cycleRepository.save(cycle);
        return mapToResponse(saved);
    }

    @Transactional
    public ImportAppraisalsResponse importCompletedAppraisals(Long cycleId, ImportAppraisalsRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementCycle cycle = cycleRepository.findByIdAndOrgId(cycleId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment cycle not found with ID: " + cycleId));

        if (cycle.getStatus() != IncrementCycleStatus.OPEN) {
            throw new BadRequestException("Increment cycle is not in OPEN status.");
        }

        List<Appraisal> appraisals = appraisalRepository.findByCycleId(request.getAppraisalCycleId());
        if (appraisals.isEmpty()) {
            appraisals = appraisalRepository.findByOrganizationId(orgId);
        }

        List<EligibilityEvaluationResponse> evaluations = new ArrayList<>();
        int eligibleCount = 0;
        int ineligibleCount = 0;

        for (Appraisal appraisal : appraisals) {
            // Critical validation: Only COMPLETED appraisals enter increment process
            if (appraisal.getStatus() != AppraisalStatus.COMPLETED && appraisal.getStatus() != AppraisalStatus.PUBLISHED) {
                continue;
            }

            Employee employee = appraisal.getEmployee();
            if (employee == null || !"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
                continue;
            }

            EligibilityEvaluationResponse eval = eligibilityService.evaluateAndRecord(
                    cycle,
                    employee,
                    appraisal,
                    null,
                    true
            );

            evaluations.add(eval);
            if (eval.isEligible()) {
                eligibleCount++;
            } else {
                ineligibleCount++;
            }
        }

        ImportAppraisalsResponse response = new ImportAppraisalsResponse();
        response.setIncrementCycleId(cycleId);
        response.setAppraisalCycleId(request.getAppraisalCycleId());
        response.setTotalEvaluated(evaluations.size());
        response.setEligibleCount(eligibleCount);
        response.setIneligibleCount(ineligibleCount);
        response.setEvaluations(evaluations);

        return response;
    }

    @Transactional
    public EligibilityEvaluationResponse calculateEmployeeEligibility(Long cycleId, Long employeeId, CheckEligibilityRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementCycle cycle = cycleRepository.findByIdAndOrgId(cycleId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment cycle not found with ID: " + cycleId));

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        Appraisal appraisal = null;
        if (request.getAppraisalId() != null) {
            appraisal = appraisalRepository.findById(request.getAppraisalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + request.getAppraisalId()));

            if (appraisal.getStatus() != AppraisalStatus.COMPLETED && appraisal.getStatus() != AppraisalStatus.PUBLISHED) {
                throw new BadRequestException("Increment processing is allowed only for completed appraisals.");
            }
        }

        boolean disciplinaryClear = request.getDisciplinaryClear() != null ? request.getDisciplinaryClear() : true;
        return eligibilityService.evaluateAndRecord(
                cycle,
                employee,
                appraisal,
                request.getProposedPercentage(),
                disciplinaryClear
        );
    }

    @Transactional(readOnly = true)
    public List<IncrementCycleResponse> getAllCycles() {
        Long orgId = TenantContext.requireOrganizationId();
        return cycleRepository.findAllByOrgId(orgId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IncrementCycleResponse getCycleById(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementCycle cycle = cycleRepository.findByIdAndOrgId(cycleId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment cycle not found with ID: " + cycleId));
        return mapToResponse(cycle);
    }

    public IncrementCycleResponse mapToResponse(IncrementCycle cycle) {
        IncrementCycleResponse resp = new IncrementCycleResponse();
        resp.setId(cycle.getId());
        resp.setName(cycle.getName());
        resp.setFinancialYear(cycle.getFinancialYear());
        resp.setStartDate(cycle.getStartDate());
        resp.setEndDate(cycle.getEndDate());
        resp.setEffectiveDate(cycle.getEffectiveDate());
        resp.setPolicyId(cycle.getPolicy() != null ? cycle.getPolicy().getId() : null);
        resp.setPolicyName(cycle.getPolicy() != null ? cycle.getPolicy().getName() : null);
        resp.setPolicyVersion(cycle.getPolicy() != null ? cycle.getPolicy().getVersion() : null);
        resp.setBudgetLimit(cycle.getBudgetLimit());
        resp.setAllocatedBudget(cycle.getAllocatedBudget());
        resp.setStatus(cycle.getStatus());
        resp.setCreatedAt(cycle.getCreatedAt());
        resp.setUpdatedAt(cycle.getUpdatedAt());
        return resp;
    }
}
