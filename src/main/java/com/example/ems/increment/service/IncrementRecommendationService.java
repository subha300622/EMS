package com.example.ems.increment.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.increment.dto.*;
import com.example.ems.increment.eligibility.IncrementEligibilityService;
import com.example.ems.increment.entity.*;
import com.example.ems.increment.repository.IncrementCycleRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncrementRecommendationService {

    @Autowired
    private IncrementRecommendationRepository recommendationRepository;

    @Autowired
    private IncrementCycleRepository cycleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private IncrementAppraisalResolver appraisalResolver;

    @Autowired
    private IncrementEligibilityService eligibilityService;

    @Transactional
    public IncrementRecommendationResponse createRecommendation(CreateRecommendationRequest request) {
        Long orgId = TenantContext.requireOrganizationId();

        IncrementCycle cycle = cycleRepository.findByIdAndOrgId(request.getCycleId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment cycle not found with ID: " + request.getCycleId()));

        if (cycle.getStatus() != IncrementCycleStatus.OPEN) {
            throw new BadRequestException("Cannot create recommendations for a cycle that is not OPEN.");
        }

        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new BadRequestException("Cannot create increment recommendation for an inactive employee.");
        }

        // Delegate conditional and secure appraisal resolution
        Appraisal appraisal = appraisalResolver.resolve(
                cycle.getPolicy(),
                employee,
                request.getAppraisalId(),
                orgId
        );

        // Prevent duplicate active recommendations for same (cycle, employee)
        Optional<IncrementRecommendation> existing = recommendationRepository.findActiveRecommendation(
                cycle.getId(),
                employee.getId(),
                appraisal != null ? appraisal.getId() : null
        );
        if (existing.isPresent()) {
            throw new BadRequestException("An active increment recommendation already exists for this employee and cycle.");
        }

        // Resolve increment percentage
        Double percentage = request.getIncrementPercentage();
        IncrementPolicy policy = cycle.getPolicy();
        if (percentage == null) {
            // Check rating band matching if appraisal is present
            if (appraisal != null && appraisal.getFinalRating() != null && policy.getBands() != null) {
                Double finalRating = appraisal.getFinalRating();
                for (IncrementPolicyBand band : policy.getBands()) {
                    if (finalRating >= band.getMinRating() && finalRating <= band.getMaxRating()) {
                        percentage = band.getIncrementPercentage();
                        break;
                    }
                }
            }
            if (percentage == null) {
                percentage = policy.getMinimumIncrementPercentage() != null ? policy.getMinimumIncrementPercentage() : 5.0;
            }
        }

        // Validate percentage limits against policy
        if (policy.getMinimumIncrementPercentage() != null && percentage < policy.getMinimumIncrementPercentage()) {
            throw new BadRequestException("Increment percentage (" + percentage + "%) cannot be less than policy minimum (" + policy.getMinimumIncrementPercentage() + "%).");
        }
        if (policy.getMaximumIncrementPercentage() != null && percentage > policy.getMaximumIncrementPercentage()) {
            throw new BadRequestException("Increment percentage (" + percentage + "%) exceeds policy maximum (" + policy.getMaximumIncrementPercentage() + "%).");
        }

        // Evaluate Eligibility
        EligibilityEvaluationResponse eval = eligibilityService.evaluateAndRecord(
                cycle,
                employee,
                appraisal,
                percentage,
                true
        );

        if (!eval.isEligible()) {
            String reasonMsg = (eval.getReasons() != null && !eval.getReasons().isEmpty())
                    ? eval.getReasons().get(0).getMessage()
                    : "Employee does not satisfy increment eligibility criteria.";
            throw new BadRequestException("Cannot create increment recommendation: " + reasonMsg);
        }

        // Calculate monetary amounts safely on backend
        BigDecimal currentSalary = employee.getAnnualSalary() != null ? employee.getAnnualSalary() : BigDecimal.ZERO;
        BigDecimal pct = BigDecimal.valueOf(percentage);
        BigDecimal incrementAmount = currentSalary.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal recommendedSalary = currentSalary.add(incrementAmount);

        LocalDate effectiveDate = request.getEffectiveDate() != null
                ? request.getEffectiveDate()
                : (cycle.getEffectiveDate() != null ? cycle.getEffectiveDate() : LocalDate.now());

        IncrementRecommendation rec = new IncrementRecommendation();
        rec.setOrganization(cycle.getOrganization());
        rec.setCycle(cycle);
        rec.setEmployee(employee);
        rec.setAppraisalId(appraisal != null ? appraisal.getId() : null);
        rec.setCurrentSalary(currentSalary);
        rec.setIncrementPercentage(pct);
        rec.setIncrementAmount(incrementAmount);
        rec.setRecommendedSalary(recommendedSalary);
        rec.setEffectiveDate(effectiveDate);
        rec.setComments(request.getComments());
        rec.setStatus(IncrementRecommendationStatus.RECOMMENDED);

        IncrementRecommendation saved = recommendationRepository.save(rec);

        // Update allocated budget on cycle
        BigDecimal newAllocated = cycle.getAllocatedBudget().add(incrementAmount);
        cycle.setAllocatedBudget(newAllocated);
        cycleRepository.save(cycle);

        return mapToResponse(saved);
    }

    @Transactional
    public IncrementRecommendationResponse updateRecommendation(Long id, UpdateRecommendationRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementRecommendation rec = recommendationRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment recommendation not found with ID: " + id));

        if (rec.getStatus() != IncrementRecommendationStatus.RECOMMENDED &&
            rec.getStatus() != IncrementRecommendationStatus.DRAFT &&
            rec.getStatus() != IncrementRecommendationStatus.SENT_BACK &&
            rec.getStatus() != IncrementRecommendationStatus.REVISED) {
            throw new BadRequestException("Cannot edit recommendation in status: " + rec.getStatus());
        }

        if (request.getIncrementPercentage() != null) {
            IncrementPolicy policy = rec.getCycle().getPolicy();
            Double percentage = request.getIncrementPercentage();
            if (policy.getMinimumIncrementPercentage() != null && percentage < policy.getMinimumIncrementPercentage()) {
                throw new BadRequestException("Increment percentage cannot be less than minimum " + policy.getMinimumIncrementPercentage() + "%");
            }
            if (policy.getMaximumIncrementPercentage() != null && percentage > policy.getMaximumIncrementPercentage()) {
                throw new BadRequestException("Increment percentage cannot exceed maximum " + policy.getMaximumIncrementPercentage() + "%");
            }

            BigDecimal currentSalary = rec.getCurrentSalary();
            BigDecimal pct = BigDecimal.valueOf(percentage);
            BigDecimal incrementAmount = currentSalary.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal recommendedSalary = currentSalary.add(incrementAmount);

            rec.setIncrementPercentage(pct);
            rec.setIncrementAmount(incrementAmount);
            rec.setRecommendedSalary(recommendedSalary);
            rec.setStatus(IncrementRecommendationStatus.REVISED);
        }

        if (request.getEffectiveDate() != null) {
            rec.setEffectiveDate(request.getEffectiveDate());
        }
        if (request.getComments() != null) {
            rec.setComments(request.getComments());
        }

        IncrementRecommendation saved = recommendationRepository.save(rec);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public IncrementRecommendationResponse getRecommendationById(Long id) {
        Long orgId = TenantContext.requireOrganizationId();
        IncrementRecommendation rec = recommendationRepository.findByIdAndOrgId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Increment recommendation not found with ID: " + id));
        return mapToResponse(rec);
    }

    @Transactional(readOnly = true)
    public List<IncrementRecommendationResponse> searchRecommendations(
            Long cycleId,
            Long employeeId,
            IncrementRecommendationStatus status,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Long orgId = TenantContext.requireOrganizationId();
        return recommendationRepository.searchRecommendations(orgId, cycleId, employeeId, status, fromDate, toDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IncrementRecommendationResponse mapToResponse(IncrementRecommendation rec) {
        IncrementRecommendationResponse resp = new IncrementRecommendationResponse();
        resp.setId(rec.getId());
        resp.setAppraisalId(rec.getAppraisalId());
        resp.setCycleId(rec.getCycle() != null ? rec.getCycle().getId() : null);
        resp.setCycleName(rec.getCycle() != null ? rec.getCycle().getName() : null);
        resp.setCurrentSalary(rec.getCurrentSalary());
        resp.setIncrementPercentage(rec.getIncrementPercentage());
        resp.setIncrementAmount(rec.getIncrementAmount());
        resp.setRecommendedSalary(rec.getRecommendedSalary());
        resp.setEffectiveDate(rec.getEffectiveDate());
        resp.setComments(rec.getComments());
        resp.setStatus(rec.getStatus());
        resp.setApprovalRequestId(rec.getApprovalRequestId());
        resp.setRejectionReason(rec.getRejectionReason());
        resp.setCreatedAt(rec.getCreatedAt());
        resp.setUpdatedAt(rec.getUpdatedAt());

        if (rec.getEmployee() != null) {
            resp.setEmployeeId(rec.getEmployee().getId());
            resp.setEmployeeName(rec.getEmployee().getFullName());
            resp.setEmployeeCode(rec.getEmployee().getEmployeeId());
            resp.setDepartment(rec.getEmployee().getDepartment());
            resp.setDesignation(rec.getEmployee().getDesignation());
            resp.setEmployee(new IncrementRecommendationResponse.EmployeeReference(rec.getEmployee().getId(), rec.getEmployee().getFullName()));
        }

        if (rec.getStatus() == IncrementRecommendationStatus.APPROVED || rec.getStatus() == IncrementRecommendationStatus.IMPLEMENTED) {
            resp.setApproval(new IncrementRecommendationResponse.ApprovalReference("FINAL_APPROVED"));
        } else if (rec.getStatus() == IncrementRecommendationStatus.REJECTED) {
            resp.setApproval(new IncrementRecommendationResponse.ApprovalReference("REJECTED"));
        } else if (rec.getStatus() == IncrementRecommendationStatus.UNDER_REVIEW) {
            resp.setApproval(new IncrementRecommendationResponse.ApprovalReference("IN_PROGRESS"));
        }

        return resp;
    }

    @Transactional(readOnly = true)
    public EmployeeIncrementHistoryResponse getEmployeeIncrementHistory(Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();
        List<IncrementRecommendation> historyList = recommendationRepository.findImplementedHistoryByEmployeeIdAndOrgId(employeeId, orgId);

        EmployeeIncrementHistoryResponse response = new EmployeeIncrementHistoryResponse();
        response.setEmployeeId(employeeId);

        List<EmployeeIncrementHistoryResponse.HistoryItemDto> items = new ArrayList<>();
        for (IncrementRecommendation rec : historyList) {
            EmployeeIncrementHistoryResponse.HistoryItemDto item = new EmployeeIncrementHistoryResponse.HistoryItemDto();
            item.setRecommendationId(rec.getId());
            item.setIncrementDate(rec.getEffectiveDate());
            item.setPreviousSalary(rec.getCurrentSalary());
            item.setIncrementPercentage(rec.getIncrementPercentage());
            item.setIncrementAmount(rec.getIncrementAmount());
            item.setRevisedSalary(rec.getRecommendedSalary());
            item.setStatus(rec.getStatus().name());
            item.setCycleName(rec.getCycle() != null ? rec.getCycle().getName() : null);
            items.add(item);
        }
        response.setHistory(items);
        return response;
    }
}
