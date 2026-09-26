package com.example.ems.incentive.service;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.incentive.dto.*;
import com.example.ems.incentive.entity.*;
import com.example.ems.incentive.repository.IncentivePolicyRepository;
import com.example.ems.incentive.repository.IncentiveRecordRepository;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.service.SalaryCalculationService;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class IncentiveCalculationService {

    private static final Logger log = LoggerFactory.getLogger(IncentiveCalculationService.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final IncentivePolicyService policyService;
    private final IncentiveEligibilityService eligibilityService;
    private final IncentivePolicyRepository policyRepository;
    private final IncentiveRecordRepository recordRepository;
    private final EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private AppraisalRepository appraisalRepository;

    @Autowired(required = false)
    private SalaryCalculationService salaryCalculationService;

    @Autowired(required = false)
    private com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public IncentiveCalculationService(IncentivePolicyService policyService,
                                       IncentiveEligibilityService eligibilityService,
                                       IncentivePolicyRepository policyRepository,
                                       IncentiveRecordRepository recordRepository,
                                       EmployeeRepository employeeRepository,
                                       @Autowired(required = false) com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService) {
        this.policyService = policyService;
        this.eligibilityService = eligibilityService;
        this.policyRepository = policyRepository;
        this.recordRepository = recordRepository;
        this.employeeRepository = employeeRepository;
        this.compensationConfigService = compensationConfigService;
    }

    public IncentiveCalculationService(IncentivePolicyService policyService,
                                       IncentiveEligibilityService eligibilityService,
                                       IncentivePolicyRepository policyRepository,
                                       IncentiveRecordRepository recordRepository,
                                       EmployeeRepository employeeRepository) {
        this(policyService, eligibilityService, policyRepository, recordRepository, employeeRepository, null);
    }

    /**
     * Stateless preview calculation of incentive without persisting.
     */
    @Transactional(readOnly = true)
    public IncentivePreviewResponse previewIncentive(IncentiveCalculateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireIncentiveEnabled(orgId);
        }
        Employee employee = employeeRepository.findByIdAndOrganizationId(request.getEmployeeId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        IncentivePolicy policy = resolvePolicy(request, employee);
        CalculationContext context = buildContext(employee, policy, request, orgId);

        IncentiveEligibilityService.EligibilityResult eligibility = eligibilityService.checkEligibility(
                employee, policy, request.getPeriodStart(), request.getPeriodEnd(),
                context.achievementPercentage, context.performanceRating);

        BigDecimal calculatedAmount = BigDecimal.ZERO;
        if (eligibility.isEligible()) {
            calculatedAmount = computeIncentiveAmount(policy, context);
        }

        IncentivePreviewResponse response = new IncentivePreviewResponse();
        response.setEmployeeId(employee.getId());
        response.setEmployeeName(employee.getFullName());
        response.setEmployeeCode(employee.getEmployeeId());
        response.setPolicyId(policy.getId());
        response.setPolicyName(policy.getName());
        response.setPolicyVersion(policy.getPolicyVersion());
        response.setPeriodStart(request.getPeriodStart());
        response.setPeriodEnd(request.getPeriodEnd());
        response.setIncentiveType(policy.getIncentiveType());
        response.setCalculationMethod(policy.getCalculationMethod());
        response.setTargetValue(context.targetValue);
        response.setAchievedValue(context.achievedValue);
        response.setAchievementPercentage(context.achievementPercentage);
        response.setPerformanceRating(context.performanceRating);
        response.setCalculatedAmount(calculatedAmount);
        response.setEligible(eligibility.isEligible());
        response.setIneligibilityReason(eligibility.isEligible() ? null : eligibility.reason());
        response.setApprovalRequired(policy.getApprovalRequired());
        return response;
    }

    /**
     * Calculates and persists an IncentiveRecord in CALCULATED status.
     */
    public IncentiveRecordResponse calculateIncentive(IncentiveCalculateRequest request) {
        return calculateAndPersist(request);
    }

    public IncentiveRecordResponse calculateAndPersist(IncentiveCalculateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireIncentiveEnabled(orgId);
        }
        Employee employee = employeeRepository.findByIdAndOrganizationId(request.getEmployeeId(), orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + request.getEmployeeId()));

        IncentivePolicy policy = resolvePolicy(request, employee);

        if (recordRepository.existsByOrganizationIdAndEmployeeIdAndPolicyIdAndPeriodStartAndPeriodEnd(
                orgId, employee.getId(), policy.getId(), request.getPeriodStart(), request.getPeriodEnd())) {
            throw new ConflictException(String.format(
                    "An incentive record already exists for employee ID %d, policy ID %d for period %s to %s.",
                    employee.getId(), policy.getId(), request.getPeriodStart(), request.getPeriodEnd()));
        }

        CalculationContext context = buildContext(employee, policy, request, orgId);

        IncentiveEligibilityService.EligibilityResult eligibility = eligibilityService.checkEligibility(
                employee, policy, request.getPeriodStart(), request.getPeriodEnd(),
                context.achievementPercentage, context.performanceRating);

        if (!eligibility.isEligible()) {
            throw new BadRequestException("Employee is not eligible for this incentive: " + eligibility.reason());
        }

        BigDecimal calculatedAmount = computeIncentiveAmount(policy, context);

        IncentiveRecord record = new IncentiveRecord();
        record.setOrganization(employee.getOrganization());
        record.setEmployee(employee);
        record.setPolicy(policy);
        record.setPolicyVersion(policy.getPolicyVersion());
        record.setPeriodStart(request.getPeriodStart());
        record.setPeriodEnd(request.getPeriodEnd());
        record.setIncentiveType(policy.getIncentiveType());
        record.setCalculationMethod(policy.getCalculationMethod());
        record.setTargetValue(context.targetValue);
        record.setAchievedValue(context.achievedValue);
        record.setAchievementPercentage(context.achievementPercentage);
        record.setPerformanceRating(context.performanceRating);
        record.setCalculatedAmount(calculatedAmount);
        record.setStatus(IncentiveStatus.CALCULATED);
        record.setPayrollStatus(IncentivePayrollStatus.PENDING);

        IncentiveRecord saved = recordRepository.save(record);
        log.info("Persisted IncentiveRecord ID={} for Emp ID={} for Period {} to {}, Amount={}",
                saved.getId(), employee.getId(), request.getPeriodStart(), request.getPeriodEnd(), calculatedAmount);

        return IncentiveRecordResponse.fromEntity(saved);
    }

    private IncentivePolicy resolvePolicy(IncentiveCalculateRequest request, Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        if (request.getPolicyId() != null) {
            return policyRepository.findByIdAndOrganizationId(request.getPolicyId(), orgId)
                    .orElseThrow(() -> new ResourceNotFoundException("Incentive policy not found with ID: " + request.getPolicyId()));
        }
        return policyService.resolveApplicablePolicy(employee, request.getPeriodEnd())
                .orElseThrow(() -> new BadRequestException("No active applicable incentive policy found for employee ID: " + employee.getId()));
    }

    private record CalculationContext(
            BigDecimal targetValue,
            BigDecimal achievedValue,
            BigDecimal achievementPercentage,
            BigDecimal performanceRating,
            BigDecimal basicSalary,
            BigDecimal grossSalary
    ) {}

    private CalculationContext buildContext(Employee employee, IncentivePolicy policy, IncentiveCalculateRequest request, Long orgId) {
        BigDecimal target = request.getAchievedValue() != null && policy.getTargetValue() != null
                ? policy.getTargetValue()
                : (policy.getTargetValue() != null ? policy.getTargetValue() : BigDecimal.ZERO);

        BigDecimal achieved = request.getAchievedValue() != null ? request.getAchievedValue() : BigDecimal.ZERO;

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
                // Find most recent completed appraisal rating
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

    private BigDecimal computeIncentiveAmount(IncentivePolicy policy, CalculationContext context) {
        BigDecimal amount = BigDecimal.ZERO;
        IncentiveCalculationMethod method = policy.getCalculationMethod();

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
            case PERCENTAGE_OF_ACHIEVEMENT -> {
                BigDecimal pct = policy.getPercentage() != null ? policy.getPercentage() : BigDecimal.ZERO;
                amount = context.achievedValue.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            case TARGET_SLAB -> {
                amount = evaluateTargetSlabs(policy, context);
            }
            case PERFORMANCE_RATING -> {
                // Example: rating (e.g. 4.5 / 5.0) * percentage of basic or fixed amount
                BigDecimal rating = context.performanceRating != null ? context.performanceRating : BigDecimal.ZERO;
                if (policy.getPercentage() != null) {
                    BigDecimal baseAmount = context.basicSalary.multiply(policy.getPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    amount = baseAmount.multiply(rating).divide(BigDecimal.valueOf(5.0), 2, RoundingMode.HALF_UP);
                } else if (policy.getFixedAmount() != null) {
                    amount = policy.getFixedAmount().multiply(rating).divide(BigDecimal.valueOf(5.0), 2, RoundingMode.HALF_UP);
                }
            }
            case FORMULA -> {
                amount = evaluateSafeFormula(policy.getFormulaExpression(), context);
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

    private BigDecimal evaluateTargetSlabs(IncentivePolicy policy, CalculationContext context) {
        if (policy.getTargetSlabsJson() == null || policy.getTargetSlabsJson().trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<IncentiveSlabTierDto> slabs;
        try {
            slabs = MAPPER.readValue(policy.getTargetSlabsJson(), new TypeReference<List<IncentiveSlabTierDto>>() {});
        } catch (Exception e) {
            log.error("Failed to parse target slabs JSON: {}", e.getMessage());
            return BigDecimal.ZERO;
        }

        BigDecimal achievementPct = context.achievementPercentage != null ? context.achievementPercentage : BigDecimal.ZERO;

        for (IncentiveSlabTierDto slab : slabs) {
            boolean minMatch = slab.getMinAchievementPercentage() == null ||
                    achievementPct.compareTo(slab.getMinAchievementPercentage()) >= 0;
            boolean maxMatch = slab.getMaxAchievementPercentage() == null ||
                    achievementPct.compareTo(slab.getMaxAchievementPercentage()) <= 0;

            if (minMatch && maxMatch) {
                if (slab.getIncentiveAmount() != null) {
                    return slab.getIncentiveAmount();
                } else if (slab.getPercentage() != null) {
                    return context.basicSalary.multiply(slab.getPercentage()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
            }
        }

        return BigDecimal.ZERO;
    }

    /**
     * Safe, restricted mathematical expression evaluator with whitelisted tokens only.
     * Prevents arbitrary code execution.
     */
    private BigDecimal evaluateSafeFormula(String formula, CalculationContext context) {
        if (formula == null || formula.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Simple and safe arithmetic substitution
        String expr = formula.toLowerCase()
                .replace("basic", context.basicSalary.toPlainString())
                .replace("gross", context.grossSalary.toPlainString())
                .replace("achieved", context.achievedValue.toPlainString())
                .replace("target", context.targetValue.toPlainString())
                .replace("rating", context.performanceRating.toPlainString());

        // Basic arithmetic calculator for simple expressions like "basic * 0.10 + 500"
        try {
            return SimpleMathEvaluator.eval(expr);
        } catch (Exception e) {
            log.warn("Formula evaluation error for expression '{}': {}", formula, e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Lightweight, self-contained recursive descent math parser for safe basic arithmetic.
     */
    private static class SimpleMathEvaluator {
        public static BigDecimal eval(final String str) {
            return new Object() {
                int pos = -1, ch;

                void nextChar() {
                    ch = (++pos < str.length()) ? str.charAt(pos) : -1;
                }

                boolean eat(int charToEat) {
                    while (ch == ' ') nextChar();
                    if (ch == charToEat) {
                        nextChar();
                        return true;
                    }
                    return false;
                }

                BigDecimal parse() {
                    nextChar();
                    BigDecimal x = parseExpression();
                    if (pos < str.length()) throw new RuntimeException("Unexpected char: " + (char) ch);
                    return x;
                }

                BigDecimal parseExpression() {
                    BigDecimal x = parseTerm();
                    for (;;) {
                        if (eat('+')) x = x.add(parseTerm());
                        else if (eat('-')) x = x.subtract(parseTerm());
                        else return x;
                    }
                }

                BigDecimal parseTerm() {
                    BigDecimal x = parseFactor();
                    for (;;) {
                        if (eat('*')) x = x.multiply(parseFactor());
                        else if (eat('/')) {
                            BigDecimal divisor = parseFactor();
                            x = divisor.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : x.divide(divisor, 4, RoundingMode.HALF_UP);
                        } else return x;
                    }
                }

                BigDecimal parseFactor() {
                    if (eat('+')) return parseFactor();
                    if (eat('-')) return parseFactor().negate();

                    BigDecimal x;
                    int startPos = this.pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                        x = new BigDecimal(str.substring(startPos, this.pos));
                    } else {
                        throw new RuntimeException("Unexpected token: " + (char) ch);
                    }
                    return x;
                }
            }.parse().setScale(2, RoundingMode.HALF_UP);
        }
    }
}
