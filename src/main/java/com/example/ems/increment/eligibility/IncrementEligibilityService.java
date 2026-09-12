package com.example.ems.increment.eligibility;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.dto.EligibilityEvaluationResponse;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementEligibilityEvaluation;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.repository.IncrementEligibilityEvaluationRepository;
import com.example.ems.increment.repository.IncrementRecommendationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
public class IncrementEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(IncrementEligibilityService.class);

    private final List<EligibilityRule> rules;
    private final IncrementRecommendationRepository recommendationRepository;
    private final IncrementEligibilityEvaluationRepository evaluationRepository;
    private final ObjectMapper objectMapper;

    public IncrementEligibilityService(
            List<EligibilityRule> rules,
            IncrementRecommendationRepository recommendationRepository,
            IncrementEligibilityEvaluationRepository evaluationRepository,
            ObjectMapper objectMapper
    ) {
        this.rules = rules;
        this.recommendationRepository = recommendationRepository;
        this.evaluationRepository = evaluationRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EligibilityEvaluationResponse evaluateAndRecord(
            IncrementCycle cycle,
            Employee employee,
            Appraisal appraisal,
            Double proposedPercentage,
            Boolean disciplinaryClear
    ) {
        EligibilityEvaluationResponse response = evaluate(cycle, employee, appraisal, proposedPercentage, disciplinaryClear);

        // Store evaluation record for audit and historical transparency
        try {
            IncrementEligibilityEvaluation eval = new IncrementEligibilityEvaluation();
            eval.setOrganization(cycle.getOrganization());
            eval.setCycle(cycle);
            eval.setEmployee(employee);
            eval.setAppraisalId(appraisal != null ? appraisal.getId() : null);
            eval.setEligible(response.isEligible());
            eval.setEligibilityStatus(response.isEligible() ? "ELIGIBLE" : "INELIGIBLE");
            eval.setFinalRating(response.getFinalRating());
            eval.setGoalAchievementPercentage(response.getGoalAchievementPercentage());
            eval.setAttendancePercentage(response.getAttendancePercentage());
            eval.setDisciplinaryClear(response.isDisciplinaryClear());
            eval.setBudgetAvailable(response.isBudgetAvailable());

            if (employee.getJoiningDate() != null) {
                LocalDate refDate = cycle.getEffectiveDate() != null ? cycle.getEffectiveDate() : LocalDate.now();
                Period period = Period.between(employee.getJoiningDate(), refDate);
                eval.setServiceMonths(period.getYears() * 12 + period.getMonths());
            }

            if (!response.isEligible() && response.getReasons() != null && !response.getReasons().isEmpty()) {
                eval.setIneligibleReason(response.getReasons().get(0).getMessage());
            }
            eval.setEligibilitySnapshot(objectMapper.writeValueAsString(response));

            evaluationRepository.save(eval);
        } catch (Exception e) {
            log.error("Failed to persist increment eligibility evaluation for employee: {}", employee.getId(), e);
        }

        return response;
    }

    public EligibilityEvaluationResponse evaluate(
            IncrementCycle cycle,
            Employee employee,
            Appraisal appraisal,
            Double proposedPercentage,
            Boolean disciplinaryClear
    ) {
        IncrementPolicy policy = cycle.getPolicy();
        BigDecimal currentSalary = employee.getAnnualSalary() != null ? employee.getAnnualSalary() : BigDecimal.ZERO;
        BigDecimal allocatedBudget = recommendationRepository.sumAllocatedAmountByCycleId(cycle.getId());

        boolean isClear = Boolean.TRUE.equals(disciplinaryClear);

        EligibilityContext context = new EligibilityContext(
                employee,
                appraisal,
                cycle,
                policy,
                currentSalary,
                allocatedBudget,
                proposedPercentage,
                isClear
        );

        List<EligibilityEvaluationResponse.IneligibleReasonDto> failureReasons = new ArrayList<>();
        boolean overallEligible = true;

        for (EligibilityRule rule : rules) {
            if (!rule.isApplicable(context)) {
                continue;
            }
            EligibilityRuleResult result = rule.evaluate(context);
            if (!result.isPassed()) {
                overallEligible = false;
                failureReasons.add(new EligibilityEvaluationResponse.IneligibleReasonDto(
                        result.getRuleName(),
                        result.getRequired(),
                        result.getActual(),
                        result.getMessage()
                ));
            }
        }

        Double finalRating = appraisal != null ? appraisal.getFinalRating() : null;

        Double goalAchievement = null;
        if (appraisal != null) {
            if (appraisal.getDeliveryManagementRating() != null) {
                goalAchievement = (appraisal.getDeliveryManagementRating() / 5.0) * 100.0;
            } else if (appraisal.getFinalRating() != null) {
                goalAchievement = (appraisal.getFinalRating() / 5.0) * 100.0;
            }
        }

        Double attendancePercentage = 100.0;
        boolean budgetAvailable = true;
        if (!failureReasons.isEmpty()) {
            for (EligibilityEvaluationResponse.IneligibleReasonDto r : failureReasons) {
                if ("BUDGET_AVAILABILITY".equals(r.getRule())) {
                    budgetAvailable = false;
                }
            }
        }

        EligibilityEvaluationResponse resp = new EligibilityEvaluationResponse();
        resp.setEmployeeId(employee.getId());
        resp.setAppraisalId(appraisal != null ? appraisal.getId() : null);
        resp.setEligible(overallEligible);
        resp.setFinalRating(finalRating);
        resp.setGoalAchievementPercentage(goalAchievement);
        resp.setAttendancePercentage(attendancePercentage);
        resp.setDisciplinaryClear(isClear);
        resp.setBudgetAvailable(budgetAvailable);

        if (overallEligible) {
            resp.setReason("Employee satisfies all increment eligibility criteria.");
        } else {
            resp.setReasons(failureReasons);
        }

        return resp;
    }
}
