package com.example.ems.finance.service;

import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.repository.PayrollRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FinanceService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    // ── 1. DASHBOARD DATA ──────────────────────────────────────────────────
    public Map<String, Object> getDashboardData() {
        List<Expense> expenses = expenseRepository.findAll();
        List<Payroll> payrollList = payrollRepository.findAll();

        BigDecimal totalExpenses = expenses.stream()
                .filter(e -> "APPROVED".equalsIgnoreCase(e.getStatus()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayroll = payrollList.stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getStatus()) || "PROCESSED".equalsIgnoreCase(p.getStatus()))
                .map(Payroll::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingExpensesCount = expenses.stream()
                .filter(e -> "PENDING".equalsIgnoreCase(e.getStatus()))
                .count();

        BigDecimal pendingExpensesAmount = expenses.stream()
                .filter(e -> "PENDING".equalsIgnoreCase(e.getStatus()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> pendingPayrollStatuses = Arrays.asList("GENERATED", "REVIEWED", "APPROVED");
        long pendingPayrollCount = payrollList.stream()
                .filter(p -> pendingPayrollStatuses.contains(p.getStatus().toUpperCase()))
                .count();

        BigDecimal pendingPayrollAmount = payrollList.stream()
                .filter(p -> pendingPayrollStatuses.contains(p.getStatus().toUpperCase()))
                .map(Payroll::getNetPay)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOutbound = totalExpenses.add(totalPayroll);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalExpenses", totalExpenses);
        data.put("totalPayroll", totalPayroll);
        data.put("pendingExpensesCount", pendingExpensesCount);
        data.put("pendingExpensesAmount", pendingExpensesAmount);
        data.put("pendingPayrollCount", pendingPayrollCount);
        data.put("pendingPayrollAmount", pendingPayrollAmount);
        data.put("totalOutbound", totalOutbound);

        return data;
    }

    // ── 2. MONTHLY ANALYTICS ────────────────────────────────────────────────
    public List<Map<String, Object>> getMonthlyAnalytics() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Expense> expenses = expenseRepository.findAll();
        List<Payroll> payrolls = payrollRepository.findAll();

        // Get last 6 months
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate targetDate = today.minusMonths(i);
            int targetMonth = targetDate.getMonthValue();
            int targetYear = targetDate.getYear();
            String monthName = targetDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            BigDecimal monthlyExpenses = expenses.stream()
                    .filter(e -> "APPROVED".equalsIgnoreCase(e.getStatus())
                            && e.getExpenseDate().getMonthValue() == targetMonth
                            && e.getExpenseDate().getYear() == targetYear)
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthlyPayroll = payrolls.stream()
                    .filter(p -> p.getMonth() == targetMonth && p.getYear() == targetYear)
                    .map(Payroll::getNetPay)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalOutbound = monthlyExpenses.add(monthlyPayroll);

            // Mock revenue = outbound * 1.5 + base buffer to make it realistic
            BigDecimal mockRevenue = totalOutbound.multiply(new BigDecimal("1.5"));
            if (mockRevenue.compareTo(BigDecimal.ZERO) == 0) {
                mockRevenue = new BigDecimal("85000.00").add(new BigDecimal(String.valueOf(targetMonth * 5000)));
            }

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("month", monthName);
            point.put("year", targetYear);
            point.put("expense", totalOutbound);
            point.put("revenue", mockRevenue);
            list.add(point);
        }

        return list;
    }

    // ── 3. RECENT TRANSACTIONS ──────────────────────────────────────────────
    public List<Map<String, Object>> getRecentTransactions() {
        List<Map<String, Object>> txns = new ArrayList<>();
        List<Expense> expenses = expenseRepository.findAll();
        List<Payroll> payrolls = payrollRepository.findAll();

        for (Expense e : expenses) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", "EXP-" + e.getId());
            map.put("type", "EXPENSE");
            map.put("title", e.getTitle());
            map.put("amount", e.getAmount());
            map.put("status", e.getStatus());
            map.put("date", e.getCreatedAt());
            map.put("reference", e.getDescription() != null ? e.getDescription() : "");
            map.put("employeeName", e.getEmployee().getFullName());
            txns.add(map);
        }

        for (Payroll p : payrolls) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", "PAY-" + p.getId());
            map.put("type", "PAYROLL");
            map.put("title", "Salary payout " + p.getMonth() + "/" + p.getYear());
            map.put("amount", p.getNetPay());
            map.put("status", p.getStatus());
            map.put("date", p.getGeneratedAt());
            map.put("reference",
                    "EMP: " + (p.getEmployee().getEmployeeId() != null ? p.getEmployee().getEmployeeId() : ""));
            map.put("employeeName", p.getEmployee().getFullName());
            txns.add(map);
        }

        // Sort by date descending
        txns.sort((a, b) -> {
            LocalDateTime dtA = (LocalDateTime) a.get("date");
            LocalDateTime dtB = (LocalDateTime) b.get("date");
            return dtB.compareTo(dtA);
        });

        // Limit to 10 items
        return txns.stream().limit(10).collect(Collectors.toList());
    }

    // ── 4. EXPENSES BY CATEGORY ─────────────────────────────────────────────
    public List<Map<String, Object>> getExpensesByCategory() {
        List<Expense> approvedExpenses = expenseRepository.findAll().stream()
                .filter(e -> "APPROVED".equalsIgnoreCase(e.getStatus()))
                .toList();

        BigDecimal total = approvedExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> grouped = approvedExpenses.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCategory().getName(),
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : grouped.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("category", entry.getKey());
            row.put("amount", entry.getValue());

            BigDecimal pct = BigDecimal.ZERO;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                pct = entry.getValue()
                        .multiply(new BigDecimal("100"))
                        .divide(total, 2, RoundingMode.HALF_UP);
            }
            row.put("percentage", pct);
            result.add(row);
        }

        // Sort categories by amount descending
        result.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));
        return result;
    }

    // ── 5. SALARY SUMMARY ───────────────────────────────────────────────────
    public Map<String, Object> getSalarySummary() {
        List<Payroll> payrolls = payrollRepository.findAll();

        BigDecimal totalBasic = payrolls.stream().map(Payroll::getBasicSalary).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAllowances = payrolls.stream().map(Payroll::getAllowances).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal totalDeductions = payrolls.stream().map(Payroll::getDeductions).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        BigDecimal totalNetPay = payrolls.stream().map(Payroll::getNetPay).reduce(BigDecimal.ZERO, BigDecimal::add);

        long employeeCount = payrolls.stream()
                .map(p -> p.getEmployee().getId())
                .distinct()
                .count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalBasicSalary", totalBasic);
        summary.put("totalAllowances", totalAllowances);
        summary.put("totalDeductions", totalDeductions);
        summary.put("totalNetPay", totalNetPay);
        summary.put("employeeCount", employeeCount);

        return summary;
    }

    // ── 6. PENDING PAYMENTS ─────────────────────────────────────────────────
    public List<Map<String, Object>> getPendingPayments() {
        List<Map<String, Object>> pending = new ArrayList<>();

        // Pending Expenses
        List<Expense> expenses = expenseRepository.findByStatus("PENDING");
        for (Expense e : expenses) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", "EXP-" + e.getId());
            map.put("type", "EXPENSE");
            map.put("title", e.getTitle());
            map.put("amount", e.getAmount());
            map.put("status", e.getStatus());
            map.put("date", e.getExpenseDate());
            map.put("recipient", e.getEmployee().getFullName());
            pending.add(map);
        }

        // Unprocessed Payroll runs
        List<String> pendingPayrollStatuses = Arrays.asList("GENERATED", "REVIEWED", "APPROVED");
        List<Payroll> payrolls = payrollRepository.findAll().stream()
                .filter(p -> pendingPayrollStatuses.contains(p.getStatus().toUpperCase()))
                .toList();

        for (Payroll p : payrolls) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", "PAY-" + p.getId());
            map.put("type", "PAYROLL");
            map.put("title", "Salary - Month " + p.getMonth() + "/" + p.getYear());
            map.put("amount", p.getNetPay());
            map.put("status", p.getStatus());
            map.put("date", p.getGeneratedAt().toLocalDate());
            map.put("recipient", p.getEmployee().getFullName());
            pending.add(map);
        }

        // Sort by amount descending
        pending.sort((a, b) -> ((BigDecimal) b.get("amount")).compareTo((BigDecimal) a.get("amount")));
        return pending;
    }

    // ── 7. CUSTOM REPORT ────────────────────────────────────────────────────
    public Map<String, Object> getCustomReport(LocalDate startDate, LocalDate endDate, String type) {
        List<Expense> expenses = expenseRepository.findAll();
        List<Payroll> payrolls = payrollRepository.findAll();

        boolean includeExpense = type == null || "ALL".equalsIgnoreCase(type) || "EXPENSE".equalsIgnoreCase(type);
        boolean includePayroll = type == null || "ALL".equalsIgnoreCase(type) || "PAYROLL".equalsIgnoreCase(type);

        List<Map<String, Object>> details = new ArrayList<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalPayroll = BigDecimal.ZERO;
        long expenseCount = 0;
        long payrollCount = 0;

        if (includeExpense) {
            List<Expense> filteredExpenses = expenses.stream()
                    .filter(e -> "APPROVED".equalsIgnoreCase(e.getStatus())
                            && !e.getExpenseDate().isBefore(startDate)
                            && !e.getExpenseDate().isAfter(endDate))
                    .toList();

            for (Expense e : filteredExpenses) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("date", e.getExpenseDate());
                map.put("type", "EXPENSE");
                map.put("title", e.getTitle());
                map.put("recipient", e.getEmployee().getFullName());
                map.put("amount", e.getAmount());
                map.put("status", e.getStatus());
                details.add(map);
                totalExpenses = totalExpenses.add(e.getAmount());
                expenseCount++;
            }
        }

        if (includePayroll) {
            List<Payroll> filteredPayrolls = payrolls.stream()
                    .filter(p -> ("PAID".equalsIgnoreCase(p.getStatus()) || "PROCESSED".equalsIgnoreCase(p.getStatus()))
                            && !p.getGeneratedAt().toLocalDate().isBefore(startDate)
                            && !p.getGeneratedAt().toLocalDate().isAfter(endDate))
                    .toList();

            for (Payroll p : filteredPayrolls) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("date", p.getGeneratedAt().toLocalDate());
                map.put("type", "PAYROLL");
                map.put("title", "Salary - Month " + p.getMonth() + "/" + p.getYear());
                map.put("recipient", p.getEmployee().getFullName());
                map.put("amount", p.getNetPay());
                map.put("status", p.getStatus());
                details.add(map);
                totalPayroll = totalPayroll.add(p.getNetPay());
                payrollCount++;
            }
        }

        // Sort details by date descending
        details.sort((a, b) -> {
            LocalDate dA = (LocalDate) a.get("date");
            LocalDate dB = (LocalDate) b.get("date");
            return dB.compareTo(dA);
        });

        BigDecimal totalOutbound = totalExpenses.add(totalPayroll);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalExpenses", totalExpenses);
        report.put("totalPayroll", totalPayroll);
        report.put("totalOutbound", totalOutbound);
        report.put("expenseCount", expenseCount);
        report.put("payrollCount", payrollCount);
        report.put("details", details);

        return report;
    }
}
