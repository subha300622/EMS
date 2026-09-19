package com.example.ems.bonus.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.bonus.dto.BonusCalculateRequest;
import com.example.ems.bonus.dto.BonusPreviewResponse;
import com.example.ems.bonus.dto.BonusRecordResponse;
import com.example.ems.bonus.entity.*;
import com.example.ems.bonus.repository.BonusPolicyRepository;
import com.example.ems.bonus.repository.BonusRecordRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.service.SalaryCalculationService;
import com.example.ems.security.context.TenantContext;
import com.example.ems.common.exception.ModuleDisabledException;
import com.example.ems.organization.service.OrganizationCompensationConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class BonusCalculationService {

    private static final Logger log = LoggerFactory.getLogger(BonusCalculationService.class);

    private final BonusPolicyService policyService;
    private final BonusEligibilityService eligibilityService;
    private final BonusPolicyRepository policyRepository;
    private final BonusRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;

    private final AppraisalRepository appraisalRepository;
    private final SalaryCalculationService salaryCalculationService;
    private final OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public BonusCalculationService(BonusPolicyService policyService,
                                   BonusEligibilityService eligibilityService,
                                   BonusPolicyRepository policyRepository,
                                   BonusRecordRepository recordRepository,
                                   EmployeeRepository employeeRepository,
                                   @Autowired(required = false) AppraisalRepository appraisalRepository,
                                   @Autowired(required = false) SalaryCalculationService salaryCalculationService,
                                   @Autowired(required = false) OrganizationCompensationConfigService compensationConfigService) {
        this.policyService = policyService;
        this.eligibilityService = eligibilityService;
        this.policyRepository = policyRepository;
        this.recordRepository = recordRepository;
        this.employeeRepository = employeeRepository;
        this.appraisalRepository = appraisalRepository;
        this.salaryCalculationService = salaryCalculationService;
        this.compensationConfigService = compensationConfigService;
    }

    /**
     * Stateless preview calculation of bonus without persisting.
     */
    @Transactional(readOnly = true)
    public BonusPreviewResponse previewBonus(BonusCalculateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null && !compensationConfigService.isBonusEnabled(orgId)) {
            throw new ModuleDisabledException("BONUS_MODULE_DISABLED", "Bonus module is not enabled for this organization");
        }
        Employee employee = employeeRepository.findByIdAndOrganizationId(request.getEmployeeId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        BonusPolicy policy = resolvePolicy(request, employee);
        CalculationContext context = buildContext(employee, policy, request, orgId);

        BonusEligibilityService.EligibilityResult eligibility = eligibilityService.checkEligibility(
                employee, policy, request.getPeriodStart(), request.getPeriodEnd(), context.performanceRating);

        BigDecimal calculatedAmount = BigDecimal.ZERO;
        if (eligibility.isEligible()) {
            calculatedAmount = computeBonusAmount(policy, context, request.getDiscretionaryAmount());
        }

        BonusPreviewResponse response = new BonusPreviewResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeName(employee.getFullName());
        response.setEmployeeCode(employee.getEmployeeId());
        response.setPolicyId(policy.getId());
        response.setPolicyName(policy.getName());
        response.setPolicyVersion(policy.getPolicyVersion());
        response.setPeriodStart(request.getPeriodStart());
        response.setPeriodEnd(request.getPeriodEnd());
        response.setBonusType(policy.getBonusType());
        response.setCalculationMethod(policy.getCalculationMethod());
        response.setTargetValue(context.targetValue);
        response.setAchievedValue(context.achievedValue);
        response.setAchievementPercentage(context.achievementPercentage);
        response.setPerformanceRating(context.performanceRating);
        response.setCalculatedAmount(calculatedAmount);
        response.setEligible(eligibility.isEligible());
        response.setIneligibilityReason(eligibility.isEligible() ? null : eligibility.reason());

        return response;
    }

    /**
     * Calculates and persists a bonus record in CALCULATED status with duplicate checking.
     */
    public BonusRecordResponse calculateAndPersist(BonusCalculateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null && !compensationConfigService.isBonusEnabled(orgId)) {
            throw new ModuleDisabledException("BONUS_MODULE_DISABLED", "Bonus module is not enabled for this organization");
        }
        Employee employee = employeeRepository.findByIdAndOrganizationId(request.getEmployeeId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        BonusPolicy policy = resolvePolicy(request, employee);

        // Duplicate protection: org + employee + policy + period
        if (recordRepository.existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
                orgId, employee.getId(), policy.getId(), request.getPeriodStart(), request.getPeriodEnd())) {
            throw new ConflictException(String.format(
                    "A bonus record already exists for employee ID %d, policy ID %d for period %s to %s.",
                    employee.getId(), policy.getId(), request.getPeriodStart(), request.getPeriodEnd()));
        }

        CalculationContext context = buildContext(employee, policy, request, orgId);

        BonusEligibilityService.EligibilityResult eligibility = eligibilityService.checkEligibility(
                employee, policy, request.getPeriodStart(), request.getPeriodEnd(), context.performanceRating);

        if (!eligibility.isEligible()) {
            throw new BadRequestException("Employee is not eligible for this bonus: " + eligibility.reason());
        }

        BigDecimal calculatedAmount = computeBonusAmount(policy, context, request.getDiscretionaryAmount());

        BonusRecord record = new BonusRecord();
        record.setOrganization(employee.getOrganization());
        record.setEmployee(employee);
        record.setPolicy(policy);
        record.setPolicyVersion(policy.getPolicyVersion());
        record.setPeriodStart(request.getPeriodStart());
        record.setPeriodEnd(request.getPeriodEnd());
        record.setBonusType(policy.getBonusType());
        record.setCalculationMethod(policy.getCalculationMethod());
        record.setTargetValue(context.targetValue);
        record.setAchievedValue(context.achievedValue);
        record.setAchievementPercentage(context.achievementPercentage);
        record.setPerformanceRating(context.performanceRating);
        record.setCalculatedAmount(calculatedAmount);
        record.setStatus(BonusStatus.CALCULATED);
        record.setPayrollStatus(BonusPayrollStatus.PENDING);

        BonusRecord saved = recordRepository.save(record);
        log.info("Persisted BonusRecord ID={} for Employee ID={} for Period {} to {}, Amount={}",
                saved.getId(), employee.getId(), request.getPeriodStart(), request.getPeriodEnd(), calculatedAmount);

        return BonusRecordResponse.fromEntity(saved);
    }

    private BonusPolicy resolvePolicy(BonusCalculateRequest request, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        if (request.getPolicyId() != null) {
            return policyRepository.findByIdAndOrganizationId(request.getPolicyId(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Bonus policy not found with ID: " + request.getPolicyId()));
        }
        return policyService.resolveApplicablePolicy(employee, request.getPeriodEnd())
                .orElseThrow(() -> new BadRequestException("No active applicable bonus policy found for employee ID: " + employee.getId()));
    }

    private record CalculationContext(
            BigDecimal targetValue,
            BigDecimal achievedValue,
            BigDecimal achievementPercentage,
            BigDecimal performanceRating,
            BigDecimal basicSalary,
            BigDecimal grossSalary
    ) {}

    private CalculationContext buildContext(Employee employee, BonusPolicy policy, BonusCalculateRequest request, Long orgId) {
        BigDecimal target = policy.getTargetValue() != null ? policy.getTargetValue() : BigDecimal.ZERO;
        BigDecimal achieved = request.getAchievementValue() != null ? request.getAchievementValue() : BigDecimal.ZERO;

        BigDecimal achievementPct = BigDecimal.ZERO;
        if (target.compareTo(BigDecimal.ZERO) > 0 && achieved.compareTo(BigDecimal.ZERO) > 0) {
            achievementPct = achieved.multiply(BigDecimal.valueOf(100)).divide(target, 2, RoundingMode.HALF_UP);
        }

        // Performance rating resolution (from Appraisal module if available, otherwise request or 0)
        BigDecimal rating = resolvePerformanceRating(employee, orgId, request.getPerformanceRating());

        // Salary resolution
        BigDecimal basicSalary = resolveBasicSalary(employee, orgId, request.getPeriodEnd());
        BigDecimal grossSalary = resolveGrossSalary(employee, orgId, request.getPeriodEnd(), basicSalary);

        return new CalculationContext(target, achieved, achievementPct, rating, basicSalary, grossSalary);
    }

    private BigDecimal resolvePerformanceRating(Employee employee, Long orgId, BigDecimal requestedRating) {
        if (requestedRating != null) {
            return requestedRating;
        }
        if (appraisalRepository != null) {
            List<Appraisal> appraisals = appraisalRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId());
            if (appraisals != null && !appraisals.isEmpty()) {
                return appraisals.stream()
                        .filter(a -> a.getStatus() == AppraisalStatus.COMPLETED && a.getFinalRating() != null)
                        .max((a1, a2) -> a1.getId().compareTo(a2.getId()))
                        .map(a -> BigDecimal.valueOf(a.getFinalRating()).setScale(2, RoundingMode.HALF_UP))
                        .orElse(BigDecimal.ZERO);
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal resolveBasicSalary(Employee employee, Long orgId, LocalDate date) {
        if (salaryCalculationService != null) {
            try {
                SalaryCalculationResponse resp = salaryCalculationService.calculateSalaryForDate(employee.getId(), date);
                if (resp != null && resp.getComponents() != null) {
                    for (var comp : resp.getComponents()) {
                        if ("BASIC".equalsIgnoreCase(comp.getComponentCode()) || "BASIC_SALARY".equalsIgnoreCase(comp.getComponentCode())) {
                            if (comp.getAmount() != null) return comp.getAmount();
                        }
                    }
                    if (resp.getGrossPay() != null && resp.getGrossPay().compareTo(BigDecimal.ZERO) > 0) {
                        return resp.getGrossPay().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not calculate dynamic basic salary for employee {}: {}. Falling back.", employee.getId(), e.getMessage());
            }
        }
        if (employee.getAnnualSalary() != null && employee.getAnnualSalary().compareTo(BigDecimal.ZERO) > 0) {
            return employee.getAnnualSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(25000.00);
    }

    private BigDecimal resolveGrossSalary(Employee employee, Long orgId, LocalDate date, BigDecimal basicSalary) {
        if (salaryCalculationService != null) {
            try {
                SalaryCalculationResponse resp = salaryCalculationService.calculateSalaryForDate(employee.getId(), date);
                if (resp != null && resp.getGrossPay() != null && resp.getGrossPay().compareTo(BigDecimal.ZERO) > 0) {
                    return resp.getGrossPay();
                }
            } catch (Exception e) {
                log.warn("Could not calculate dynamic gross salary for employee {}: {}. Falling back.", employee.getId(), e.getMessage());
            }
        }
        if (employee.getAnnualSalary() != null && employee.getAnnualSalary().compareTo(BigDecimal.ZERO) > 0) {
            return employee.getAnnualSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        }
        return basicSalary.multiply(BigDecimal.valueOf(2));
    }

    private BigDecimal computeBonusAmount(BonusPolicy policy, CalculationContext context, BigDecimal discretionaryAmount) {
        BigDecimal amount = BigDecimal.ZERO;
        BonusCalculationMethod method = policy.getCalculationMethod();

        switch (method) {
            case FIXED_AMOUNT -> {
                amount = policy.getFixedAmount() != null ? policy.getFixedAmount() : BigDecimal.ZERO;
            }
            case PERCENTAGE_OF_BASIC -> {
                BigDecimal pct = policy.getPercentage() != null ? policy.getPercentage() : BigDecimal.ZERO;
                amount = context.basicSalary.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            case PERCENTAGE_OF_GROSS -> {
                BigDecimal pct = policy.getPercentage() != null ? policy.getPercentage() : BigDecimal.ZERO;
                amount = context.grossSalary.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            case PERFORMANCE_BASED -> {
                BigDecimal rating = context.performanceRating != null ? context.performanceRating : BigDecimal.ZERO;
                if (policy.getPercentage() != null && policy.getPercentage().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal base = context.basicSalary.multiply(policy.getPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    // Scale proportionally by rating (assuming 5.0 scale) if rating is provided
                    if (rating.compareTo(BigDecimal.ZERO) > 0) {
                        amount = base.multiply(rating).divide(BigDecimal.valueOf(5.0), 2, RoundingMode.HALF_UP);
                    } else {
                        amount = base;
                    }
                } else if (policy.getFixedAmount() != null && policy.getFixedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    if (rating.compareTo(BigDecimal.ZERO) > 0) {
                        amount = policy.getFixedAmount().multiply(rating).divide(BigDecimal.valueOf(5.0), 2, RoundingMode.HALF_UP);
                    } else {
                        amount = policy.getFixedAmount();
                    }
                }
            }
            case DISCRETIONARY -> {
                if (discretionaryAmount != null) {
                    amount = discretionaryAmount;
                } else if (policy.getFixedAmount() != null) {
                    amount = policy.getFixedAmount();
                }
            }
        }

        // Apply minimum and maximum caps
        if (policy.getMinimumAmount() != null && amount.compareTo(policy.getMinimumAmount()) < 0) {
            amount = policy.getMinimumAmount();
        }
        if (policy.getMaximumAmount() != null && amount.compareTo(policy.getMaximumAmount()) > 0) {
            amount = policy.getMaximumAmount();
        }

        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
