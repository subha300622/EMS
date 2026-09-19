package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.appraisal.repository.SalaryRevisionRepository;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppraisalIncrementService {

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private IncrementRepository incrementRepository;

    @Autowired
    private SalaryRevisionRepository salaryRevisionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AppraisalConfigurationExtendedService configExtendedService;

    @Autowired
    private AppraisalHistoryService historyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public IncrementCalculationPreviewDto calculateIncrementPreview(Long appraisalId) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        Employee employee = appraisal.getEmployee();
        if (employee == null) {
            throw new IllegalStateException("Appraisal is not associated with an employee");
        }

        BigDecimal currentSalary = employee.getAnnualSalary() != null
                ? employee.getAnnualSalary()
                : BigDecimal.valueOf(600000.00);

        Double rating = appraisal.getFinalRating();
        if (rating == null) {
            rating = appraisal.getSelfRating() != null ? appraisal.getSelfRating() : 3.0;
        }

        IncrementPolicyDto policy = resolvePolicy(appraisal);
        IncrementRuleDto matchingRule = null;
        if (policy != null && policy.getRules() != null) {
            for (IncrementRuleDto rule : policy.getRules()) {
                if (rule.getActive() != null && rule.getActive()) {
                    if (rating >= rule.getMinRating() && rating <= rule.getMaxRating()) {
                        matchingRule = rule;
                        break;
                    }
                }
            }
        }

        double incPct = matchingRule != null && matchingRule.getIncrementPercentage() != null
                ? matchingRule.getIncrementPercentage()
                : 10.0;
        double bonusPct = matchingRule != null && matchingRule.getBonusPercentage() != null
                ? matchingRule.getBonusPercentage()
                : 0.0;
        boolean isEligible = matchingRule == null || (matchingRule.getEligible() != null && matchingRule.getEligible());

        BigDecimal incAmount = currentSalary.multiply(BigDecimal.valueOf(incPct))
                .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);
        BigDecimal bonusAmount = currentSalary.multiply(BigDecimal.valueOf(bonusPct))
                .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);
        BigDecimal proposedSalary = currentSalary.add(incAmount);

        String category = appraisal.getPerformanceCategory() != null
                ? appraisal.getPerformanceCategory()
                : (matchingRule != null ? matchingRule.getPerformanceCategoryName() : "MEETS_EXPECTATIONS");

        IncrementCalculationPreviewDto preview = new IncrementCalculationPreviewDto();
        preview.setAppraisalId(appraisal.getId());
        preview.setEmployeeId(employee.getId());
        preview.setEmployeeName(employee.getFullName());
        preview.setFinalRating(rating);
        preview.setPerformanceCategory(category);
        preview.setCurrentSalary(currentSalary);
        preview.setSuggestedIncrementPercentage(incPct);
        preview.setBonusPercentage(bonusPct);
        preview.setIncrementAmount(incAmount);
        preview.setBonusAmount(bonusAmount);
        preview.setProposedSalary(proposedSalary);
        preview.setEligible(isEligible);
        preview.setPolicyName(policy != null ? policy.getName() : "Standard Increment Policy");
        preview.setEffectiveDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));

        return preview;
    }

    @Transactional
    public AppraisalIncrementResponseDto approveIncrement(Long appraisalId, ApproveIncrementRequestDto dto, Employee approver) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        Employee employee = appraisal.getEmployee();
        BigDecimal currentSalary = employee.getAnnualSalary() != null
                ? employee.getAnnualSalary()
                : BigDecimal.valueOf(600000.00);

        Double incPct = dto.getApprovedIncrementPercentage();
        BigDecimal incAmount = currentSalary.multiply(BigDecimal.valueOf(incPct))
                .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);
        BigDecimal newSalary = currentSalary.add(incAmount);

        Double bonusPct = dto.getBonusPercentage() != null ? dto.getBonusPercentage() : 0.0;
        BigDecimal bonusAmount = currentSalary.multiply(BigDecimal.valueOf(bonusPct))
                .divide(BigDecimal.valueOf(100.0), 2, RoundingMode.HALF_UP);

        LocalDate effectiveDate = dto.getEffectiveDate() != null
                ? dto.getEffectiveDate()
                : LocalDate.now().plusMonths(1).withDayOfMonth(1);

        Increment inc = incrementRepository.findByAppraisalId(appraisalId)
                .orElseGet(() -> {
                    Increment i = new Increment();
                    i.setAppraisal(appraisal);
                    i.setEmployee(employee);
                    i.setCreatedAt(LocalDateTime.now());
                    return i;
                });

        inc.setCurrentSalary(currentSalary);
        inc.setIncrementPercentage(BigDecimal.valueOf(incPct).setScale(2, RoundingMode.HALF_UP));
        inc.setIncrementAmount(incAmount);
        inc.setNewSalary(newSalary);
        inc.setEffectiveDate(effectiveDate);
        inc.setStatus("APPROVED");
        inc.setApprovedBy(approver);
        inc.setApprovedAt(LocalDateTime.now());
        inc.setReason(dto.getRemarks() != null ? dto.getRemarks() : "Performance appraisal increment approved");
        inc.setUpdatedAt(LocalDateTime.now());

        Increment saved = incrementRepository.save(inc);

        historyService.recordHistory(
                appraisal,
                employee,
                "INCREMENT_APPROVED",
                "PENDING",
                "APPROVED",
                approver,
                "Approved increment: " + incPct + "% (New Salary: " + newSalary + ")"
        );

        return mapToDto(saved, bonusPct, bonusAmount);
    }

    @Transactional
    public AppraisalIncrementResponseDto applyIncrement(Long appraisalId, ApplyIncrementRequestDto dto, Employee actor) {
        Long orgId = TenantContext.requireOrganizationId();
        Appraisal appraisal = appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        Increment inc = incrementRepository.findByAppraisalId(appraisalId)
                .orElseThrow(() -> new ResourceNotFoundException("No increment record found for appraisal ID: " + appraisalId));

        if ("APPLIED".equalsIgnoreCase(inc.getStatus())) {
            // Idempotent return if already applied
            return mapToDto(inc, 0.0, BigDecimal.ZERO);
        }

        if (!"APPROVED".equalsIgnoreCase(inc.getStatus())) {
            throw new IllegalStateException("Cannot apply increment with status '" + inc.getStatus() + "'. Increment must be APPROVED first.");
        }

        Employee employee = inc.getEmployee();
        BigDecimal oldSalary = employee.getAnnualSalary() != null ? employee.getAnnualSalary() : inc.getCurrentSalary();

        // Update employee annual salary directly
        employee.setAnnualSalary(inc.getNewSalary());
        employeeRepository.save(employee);

        // Record legacy SalaryRevision record for audit
        SalaryRevision rev = new SalaryRevision();
        rev.setEmployee(employee);
        rev.setPreviousSalary(oldSalary);
        rev.setNewSalary(inc.getNewSalary());
        rev.setChangePercentage(inc.getIncrementPercentage());
        rev.setEffectiveDate(dto.getEffectiveDate() != null ? dto.getEffectiveDate() : inc.getEffectiveDate());
        rev.setReason(dto.getRemarks() != null ? dto.getRemarks() : inc.getReason());
        rev.setCreatedAt(LocalDateTime.now());
        salaryRevisionRepository.save(rev);

        inc.setStatus("APPLIED");
        inc.setAppliedAt(LocalDateTime.now());
        inc.setUpdatedAt(LocalDateTime.now());
        Increment saved = incrementRepository.save(inc);

        historyService.recordHistory(
                appraisal,
                employee,
                "INCREMENT_APPLIED",
                "APPROVED",
                "APPLIED",
                actor,
                "Applied increment to payroll: " + inc.getIncrementPercentage() + "% (New Salary: " + inc.getNewSalary() + ")"
        );

        return mapToDto(saved, 0.0, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public AppraisalIncrementResponseDto getIncrementByAppraisalId(Long appraisalId) {
        Long orgId = TenantContext.requireOrganizationId();
        appraisalRepository.findByIdAndOrganizationId(appraisalId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal not found with ID: " + appraisalId));

        Increment inc = incrementRepository.findByAppraisalId(appraisalId)
                .orElseThrow(() -> new ResourceNotFoundException("No increment record found for appraisal ID: " + appraisalId));

        return mapToDto(inc, 0.0, BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<AppraisalIncrementResponseDto> getIncrementHistory(Long employeeId) {
        Long orgId = TenantContext.requireOrganizationId();
        List<Increment> list = (employeeId != null)
                ? incrementRepository.findByEmployeeIdAndOrganizationId(employeeId, orgId)
                : incrementRepository.findByOrganizationId(orgId);

        return list.stream()
                .map(i -> mapToDto(i, 0.0, BigDecimal.ZERO))
                .collect(Collectors.toList());
    }

    private IncrementPolicyDto resolvePolicy(Appraisal appraisal) {
        if (appraisal.getCycle() != null && appraisal.getCycle().getConfigurationVersion() != null) {
            String json = appraisal.getCycle().getConfigurationVersion().getSnapshotJson();
            if (json != null) {
                try {
                    AppraisalConfigurationSnapshotDto snapshot = objectMapper.readValue(json, AppraisalConfigurationSnapshotDto.class);
                    if (snapshot.getIncrementPolicy() != null) {
                        return snapshot.getIncrementPolicy();
                    }
                } catch (Exception ignored) {}
            }
        }
        return configExtendedService.getIncrementPolicy();
    }

    private AppraisalIncrementResponseDto mapToDto(Increment i, Double bonusPct, BigDecimal bonusAmount) {
        AppraisalIncrementResponseDto dto = new AppraisalIncrementResponseDto();
        dto.setId(i.getId());
        dto.setAppraisalId(i.getAppraisal() != null ? i.getAppraisal().getId() : null);
        if (i.getEmployee() != null) {
            dto.setEmployeeId(i.getEmployee().getId());
            dto.setEmployeeName(i.getEmployee().getFullName());
        }
        dto.setCurrentSalary(i.getCurrentSalary());
        dto.setIncrementPercentage(i.getIncrementPercentage() != null ? i.getIncrementPercentage().doubleValue() : 0.0);
        dto.setIncrementAmount(i.getIncrementAmount());
        dto.setBonusPercentage(bonusPct != null ? bonusPct : 0.0);
        dto.setBonusAmount(bonusAmount != null ? bonusAmount : BigDecimal.ZERO);
        dto.setNewSalary(i.getNewSalary());
        dto.setEffectiveDate(i.getEffectiveDate());
        dto.setStatus(i.getStatus());
        if (i.getApprovedBy() != null) {
            dto.setApprovedByName(i.getApprovedBy().getFullName());
        }
        dto.setApprovedAt(i.getApprovedAt());
        dto.setAppliedAt(i.getAppliedAt());
        dto.setRemarks(i.getReason());
        dto.setCreatedAt(i.getCreatedAt());
        return dto;
    }
}
