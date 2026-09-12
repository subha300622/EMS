package com.example.ems.payroll.integration;

import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.entity.ExpenseStatus;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.payroll.dto.ReimbursementPeriodSummaryDto;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class ReimbursementPayrollAdapter {

    private final ExpenseRepository expenseRepository;

    public ReimbursementPayrollAdapter(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public ReimbursementPeriodSummaryDto getReimbursementSummary(Long employeeId, Long organizationId,
                                                                 LocalDate periodStart, LocalDate periodEnd) {
        List<Expense> eligibleExpenses = expenseRepository.findEligibleSalaryReimbursements(
                employeeId,
                ExpenseStatus.APPROVED,
                "NOT_PAID",
                "SALARY_PAYROLL",
                periodEnd
        );

        if (eligibleExpenses == null || eligibleExpenses.isEmpty()) {
            return new ReimbursementPeriodSummaryDto(BigDecimal.ZERO, new ArrayList<>(), new ArrayList<>());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Long> ids = new ArrayList<>();
        List<String> numbers = new ArrayList<>();

        for (Expense exp : eligibleExpenses) {
            if (exp.getAmount() != null) {
                totalAmount = totalAmount.add(exp.getAmount());
            }
            ids.add(exp.getId());
            if (exp.getExpenseNumber() != null) {
                numbers.add(exp.getExpenseNumber());
            }
        }

        return new ReimbursementPeriodSummaryDto(totalAmount, ids, numbers);
    }

    @Transactional
    public void markReimbursementsProcessed(List<Long> expenseIds, Long payrollRunId) {
        if (expenseIds == null || expenseIds.isEmpty()) return;

        List<Expense> expenses = expenseRepository.findAllById(expenseIds);
        for (Expense exp : expenses) {
            exp.setReimbursementStatus("PROCESSED");
            exp.setTransactionReference("PAYROLL_RUN_" + payrollRunId);
            expenseRepository.save(exp);
        }
    }
}
