package com.example.ems.payroll.service;

import com.example.ems.appraisal.entity.Increment;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.common.service.EmailService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.*;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.entity.Payslip;
import com.example.ems.payroll.repository.PayrollRepository;
import com.example.ems.payroll.repository.PayslipRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MyPayslipService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private PayslipRepository payslipRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private IncrementRepository incrementRepository;

    @Autowired
    private EmailService emailService;

    private static final String[] MONTHS = {
        "", "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    private String getMonthName(int month) {
        if (month >= 1 && month <= 12) {
            return MONTHS[month];
        }
        return "Unknown";
    }

    private int getMonthNumber(String monthStr) {
        if (monthStr == null || monthStr.trim().isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(monthStr.trim());
        } catch (NumberFormatException e) {
            String clean = monthStr.trim().toLowerCase();
            for (int i = 1; i < MONTHS.length; i++) {
                if (MONTHS[i].toLowerCase().startsWith(clean)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Transactional(readOnly = true)
    public MyPayslipDashboardResponse getPayslipDashboard(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        // Employee Info
        MyPayslipDashboardResponse.EmployeeInfo empInfo = new MyPayslipDashboardResponse.EmployeeInfo(
                emp.getId(),
                emp.getEmployeeId(),
                emp.getFullName(),
                emp.getDesignation(),
                emp.getDepartment()
        );

        // Salary Overview
        BigDecimal annual = emp.getAnnualSalary() != null ? emp.getAnnualSalary() : BigDecimal.ZERO;
        MyPayslipDashboardResponse.CTCInfo ctc = new MyPayslipDashboardResponse.CTCInfo(
                annual, "INR", "INR " + annual.toString()
        );

        List<Increment> increments = incrementRepository.findByEmployeeId(emp.getId()).stream()
                .filter(inc -> "APPLIED".equalsIgnoreCase(inc.getStatus()))
                .sorted(Comparator.comparing(Increment::getEffectiveDate).reversed())
                .collect(Collectors.toList());

        LocalDate lastRevision = increments.isEmpty() ? emp.getJoiningDate() : increments.get(0).getEffectiveDate();
        MyPayslipDashboardResponse.SalaryOverviewInfo salaryOverview = new MyPayslipDashboardResponse.SalaryOverviewInfo(
                ctc, lastRevision
        );

        // Latest Payroll Info
        List<Payroll> payrolls = payrollRepository.findByEmployeeId(emp.getId()).stream()
                .filter(p -> "PROCESSED".equalsIgnoreCase(p.getStatus()) || "PAID".equalsIgnoreCase(p.getStatus()))
                .sorted(Comparator.comparing(Payroll::getYear).thenComparing(Payroll::getMonth).reversed())
                .collect(Collectors.toList());

        MyPayslipDashboardResponse.LatestPayrollInfo latestPayroll = null;
        if (!payrolls.isEmpty()) {
            Payroll p = payrolls.get(0);
            BigDecimal gross = p.getBasicSalary().add(p.getAllowances());
            latestPayroll = new MyPayslipDashboardResponse.LatestPayrollInfo(
                    getMonthName(p.getMonth()) + " " + p.getYear(),
                    gross,
                    p.getDeductions(),
                    p.getNetPay(),
                    p.getProcessedAt() != null ? p.getProcessedAt().toLocalDate() : null,
                    p.getStatus()
            );
        }

        // Statistics
        List<Payslip> payslips = payslipRepository.findByPayrollEmployeeId(emp.getId());
        MyPayslipDashboardResponse.StatisticsInfo stats = new MyPayslipDashboardResponse.StatisticsInfo(
                payslips.size(),
                payrolls.isEmpty() ? "FY 2025-26" : payrolls.get(0).getFinancialYear()
        );

        return new MyPayslipDashboardResponse(empInfo, salaryOverview, latestPayroll, stats);
    }

    @Transactional(readOnly = true)
    public MyPayslipHistoryResponse getPayslipHistory(String email, String financialYear, String month, String status, int page, int size, String sort) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        List<Payslip> allPayslips = payslipRepository.findByPayrollEmployeeId(emp.getId());

        // Filtering
        List<Payslip> filtered = allPayslips.stream()
                .filter(ps -> {
                    Payroll pr = ps.getPayroll();
                    if (financialYear != null && !financialYear.trim().isEmpty() && !financialYear.equalsIgnoreCase(pr.getFinancialYear())) {
                        return false;
                    }
                    if (month != null && !month.trim().isEmpty()) {
                        int mNum = getMonthNumber(month);
                        if (mNum != pr.getMonth()) {
                            return false;
                        }
                    }
                    if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase(pr.getStatus())) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Sorting
        boolean desc = sort == null || sort.toLowerCase().contains("desc");
        Comparator<Payslip> comp = Comparator.comparing((Payslip ps) -> ps.getPayroll().getYear())
                .thenComparing(ps -> ps.getPayroll().getMonth());
        if (desc) {
            comp = comp.reversed();
        }
        filtered.sort(comp);

        // Pagination
        int total = filtered.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int start = page * size;
        int end = Math.min(start + size, total);

        List<Payslip> paginated = (start >= total || start < 0) ? Collections.emptyList() : filtered.subList(start, end);

        List<MyPayslipHistoryResponse.PayslipItem> items = paginated.stream()
                .map(ps -> {
                    Payroll pr = ps.getPayroll();
                    BigDecimal gross = pr.getBasicSalary().add(pr.getAllowances());
                    MyPayslipHistoryResponse.ActionInfo actions = new MyPayslipHistoryResponse.ActionInfo(true, true);
                    return new MyPayslipHistoryResponse.PayslipItem(
                            ps.getId(),
                            ps.getPayslipNumber(),
                            getMonthName(pr.getMonth()) + " " + pr.getYear(),
                            gross,
                            pr.getDeductions(),
                            pr.getNetPay(),
                            pr.getProcessedAt() != null ? pr.getProcessedAt().toLocalDate() : null,
                            pr.getStatus(),
                            actions
                    );
                })
                .collect(Collectors.toList());

        MyPayslipHistoryResponse.PaginationInfo pagination = new MyPayslipHistoryResponse.PaginationInfo(
                page, size, total, totalPages
        );

        return new MyPayslipHistoryResponse(items, pagination);
    }

    @Transactional(readOnly = true)
    public MyPayslipDetailsResponse getPayslipDetails(String email, Long payslipId) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        Payslip ps = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found with ID: " + payslipId));

        if (!ps.getPayroll().getEmployee().getId().equals(emp.getId())) {
            throw new IllegalArgumentException("Access Denied: Payslip does not belong to you.");
        }

        Payroll pr = ps.getPayroll();

        MyPayslipDetailsResponse.EmployeeInfo empInfo = new MyPayslipDetailsResponse.EmployeeInfo(
                emp.getId(),
                emp.getFullName(),
                emp.getEmployeeId(),
                emp.getDesignation(),
                emp.getDepartment()
        );

        MyPayslipDetailsResponse.SalaryPeriodInfo periodInfo = new MyPayslipDetailsResponse.SalaryPeriodInfo(
                getMonthName(pr.getMonth()),
                pr.getYear(),
                pr.getWorkingDays(),
                pr.getPaidDays()
        );

        List<MyPayslipDetailsResponse.ComponentAmount> earnings = List.of(
                new MyPayslipDetailsResponse.ComponentAmount("Basic Salary", pr.getBasicSalary()),
                new MyPayslipDetailsResponse.ComponentAmount("House Rent Allowance (HRA)", pr.getHra()),
                new MyPayslipDetailsResponse.ComponentAmount("Special Allowance", pr.getSpecialAllowance()),
                new MyPayslipDetailsResponse.ComponentAmount("Performance Bonus", pr.getPerformanceBonus())
        );

        List<MyPayslipDetailsResponse.ComponentAmount> deductions = List.of(
                new MyPayslipDetailsResponse.ComponentAmount("Provident Fund (PF)", pr.getProvidentFund()),
                new MyPayslipDetailsResponse.ComponentAmount("Professional Tax", pr.getProfessionalTax()),
                new MyPayslipDetailsResponse.ComponentAmount("Income Tax (TDS)", pr.getIncomeTax())
        );

        BigDecimal gross = pr.getBasicSalary().add(pr.getAllowances());
        MyPayslipDetailsResponse.SummaryInfo summary = new MyPayslipDetailsResponse.SummaryInfo(
                gross,
                pr.getDeductions(),
                pr.getNetPay()
        );

        MyPayslipDetailsResponse.PaymentDetailsInfo payment = new MyPayslipDetailsResponse.PaymentDetailsInfo(
                pr.getStatus(),
                pr.getPaymentMode(),
                pr.getTransactionReference()
        );

        return new MyPayslipDetailsResponse(
                ps.getId(),
                ps.getPayslipNumber(),
                empInfo,
                periodInfo,
                earnings,
                deductions,
                summary,
                payment
        );
    }

    @Transactional(readOnly = true)
    public MyPayslipPreviewResponse previewPayslip(String email, Long payslipId) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        Payslip ps = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found with ID: " + payslipId));

        if (!ps.getPayroll().getEmployee().getId().equals(emp.getId())) {
            throw new IllegalArgumentException("Access Denied: Payslip does not belong to you.");
        }

        String previewUrl = "/api/v1/my-payslips/" + payslipId + "/download";
        return new MyPayslipPreviewResponse(
                ps.getId(),
                ps.getPayslipNumber(),
                previewUrl,
                "1 Hour"
        );
    }

    @Transactional(readOnly = true)
    public byte[] getPayslipPdf(String email, Long payslipId) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        Payslip ps = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found with ID: " + payslipId));

        if (!ps.getPayroll().getEmployee().getId().equals(emp.getId())) {
            throw new IllegalArgumentException("Access Denied: Payslip does not belong to you.");
        }

        Payroll pr = ps.getPayroll();
        String period = getMonthName(pr.getMonth()) + " " + pr.getYear();
        BigDecimal gross = pr.getBasicSalary().add(pr.getAllowances());

        String pdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>\nendobj\n" +
                "4 0 obj\n<< /Length 1000 >>\nstream\n" +
                "BT\n/F1 12 Tf\n70 700 Td\n(Payslip - " + ps.getPayslipNumber() + ") Tj\n" +
                "0 -20 Td\n(Employee: " + emp.getFullName() + " [" + emp.getEmployeeId() + "]) Tj\n" +
                "0 -20 Td\n(Designation: " + emp.getDesignation() + " | Department: " + emp.getDepartment() + ") Tj\n" +
                "0 -20 Td\n(Period: " + period + " | Financial Year: " + pr.getFinancialYear() + ") Tj\n" +
                "0 -20 Td\n(Working Days: " + pr.getWorkingDays() + " | Paid Days: " + pr.getPaidDays() + ") Tj\n" +
                "0 -30 Td\n(Earnings:) Tj\n" +
                "0 -20 Td\n(  Basic Salary: INR " + pr.getBasicSalary() + ") Tj\n" +
                "0 -20 Td\n(  HRA: INR " + pr.getHra() + ") Tj\n" +
                "0 -20 Td\n(  Special Allowance: INR " + pr.getSpecialAllowance() + ") Tj\n" +
                "0 -20 Td\n(  Performance Bonus: INR " + pr.getPerformanceBonus() + ") Tj\n" +
                "0 -30 Td\n(Deductions:) Tj\n" +
                "0 -20 Td\n(  Provident Fund: INR " + pr.getProvidentFund() + ") Tj\n" +
                "0 -20 Td\n(  Professional Tax: INR " + pr.getProfessionalTax() + ") Tj\n" +
                "0 -20 Td\n(  Income Tax (TDS): INR " + pr.getIncomeTax() + ") Tj\n" +
                "0 -30 Td\n(Summary:) Tj\n" +
                "0 -20 Td\n(  Gross Salary: INR " + gross + ") Tj\n" +
                "0 -20 Td\n(  Total Deductions: INR " + pr.getDeductions() + ") Tj\n" +
                "0 -20 Td\n(  Net Pay: INR " + pr.getNetPay() + ") Tj\n" +
                "0 -30 Td\n(Payment Details:) Tj\n" +
                "0 -20 Td\n(  Status: " + pr.getStatus() + ") Tj\n" +
                "0 -20 Td\n(  Payment Mode: " + pr.getPaymentMode() + ") Tj\n" +
                "0 -20 Td\n(  Reference: " + (pr.getTransactionReference() != null ? pr.getTransactionReference() : "N/A") + ") Tj\n" +
                "ET\n" +
                "endstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000212 00000 n\ntrailer\n<< /Size 5 >>\nstartxref\n313\n%%EOF";

        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    @Transactional(readOnly = true)
    public AnnualSalaryStatementResponse getAnnualStatement(String email, String financialYear) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        List<Payroll> payrolls = payrollRepository.findByEmployeeId(emp.getId()).stream()
                .filter(p -> "PROCESSED".equalsIgnoreCase(p.getStatus()) || "PAID".equalsIgnoreCase(p.getStatus()))
                .filter(p -> financialYear == null || financialYear.trim().isEmpty() || financialYear.equalsIgnoreCase(p.getFinancialYear()))
                .collect(Collectors.toList());

        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Payroll p : payrolls) {
            BigDecimal gross = p.getBasicSalary().add(p.getAllowances());
            totalGross = totalGross.add(gross);
            totalDeductions = totalDeductions.add(p.getDeductions());
            totalNet = totalNet.add(p.getNetPay());
        }

        AnnualSalaryStatementResponse.SalarySummaryInfo summary = new AnnualSalaryStatementResponse.SalarySummaryInfo(
                totalGross,
                totalDeductions,
                totalNet,
                payrolls.size()
        );

        String fy = (financialYear == null || financialYear.trim().isEmpty()) ? "FY 2025-26" : financialYear;
        return new AnnualSalaryStatementResponse(fy, summary, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public byte[] getAnnualStatementPdf(String email, String financialYear) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        AnnualSalaryStatementResponse resp = getAnnualStatement(email, financialYear);
        AnnualSalaryStatementResponse.SalarySummaryInfo summary = resp.getSalarySummary();

        String pdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << >> >>\nendobj\n" +
                "4 0 obj\n<< /Length 1000 >>\nstream\n" +
                "BT\n/F1 12 Tf\n70 700 Td\n(Annual Salary Statement - " + resp.getFinancialYear() + ") Tj\n" +
                "0 -20 Td\n(Employee: " + emp.getFullName() + " [" + emp.getEmployeeId() + "]) Tj\n" +
                "0 -20 Td\n(Designation: " + emp.getDesignation() + " | Department: " + emp.getDepartment() + ") Tj\n" +
                "0 -30 Td\n(Summary Overview:) Tj\n" +
                "0 -20 Td\n(  Months Processed: " + summary.getMonthsProcessed() + ") Tj\n" +
                "0 -20 Td\n(  Total Gross Salary: INR " + summary.getTotalGrossSalary() + ") Tj\n" +
                "0 -20 Td\n(  Total Deductions: INR " + summary.getTotalDeductions() + ") Tj\n" +
                "0 -20 Td\n(  Total Net Salary: INR " + summary.getTotalNetSalary() + ") Tj\n" +
                "0 -40 Td\n(This is a computer-generated summary and does not require a physical signature.) Tj\n" +
                "ET\n" +
                "endstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000212 00000 n\ntrailer\n<< /Size 5 >>\nstartxref\n313\n%%EOF";

        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    @Transactional(readOnly = true)
    public MySalaryRevisionsResponse getSalaryRevisionHistory(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        BigDecimal annual = emp.getAnnualSalary() != null ? emp.getAnnualSalary() : BigDecimal.ZERO;
        MySalaryRevisionsResponse.CurrentCTCInfo current = new MySalaryRevisionsResponse.CurrentCTCInfo(annual, "INR");

        List<Increment> increments = incrementRepository.findByEmployeeId(emp.getId()).stream()
                .filter(inc -> "APPLIED".equalsIgnoreCase(inc.getStatus()))
                .sorted(Comparator.comparing(Increment::getEffectiveDate).reversed())
                .collect(Collectors.toList());

        List<MySalaryRevisionsResponse.RevisionItem> history = increments.stream()
                .map(inc -> new MySalaryRevisionsResponse.RevisionItem(
                        inc.getId(),
                        inc.getEffectiveDate(),
                        inc.getCurrentSalary(),
                        inc.getNewSalary(),
                        inc.getIncrementPercentage(),
                        inc.getReason() != null ? inc.getReason() : "Annual Appraisal Increment"
                ))
                .collect(Collectors.toList());

        return new MySalaryRevisionsResponse(current, history);
    }

    @Transactional(readOnly = true)
    public TaxSummaryResponse getTaxSummary(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        List<Payroll> payrolls = payrollRepository.findByEmployeeId(emp.getId()).stream()
                .filter(p -> "PROCESSED".equalsIgnoreCase(p.getStatus()) || "PAID".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal pf = BigDecimal.ZERO;
        BigDecimal pt = BigDecimal.ZERO;
        String financialYear = "FY 2025-26";

        if (!payrolls.isEmpty()) {
            financialYear = payrolls.get(0).getFinancialYear();
            for (Payroll p : payrolls) {
                tax = tax.add(p.getIncomeTax() != null ? p.getIncomeTax() : BigDecimal.ZERO);
                pf = pf.add(p.getProvidentFund() != null ? p.getProvidentFund() : BigDecimal.ZERO);
                pt = pt.add(p.getProfessionalTax() != null ? p.getProfessionalTax() : BigDecimal.ZERO);
            }
        }

        TaxSummaryResponse.TaxDetailsInfo details = new TaxSummaryResponse.TaxDetailsInfo(tax, pf, pt);
        return new TaxSummaryResponse(financialYear, details);
    }

    @Transactional
    public EmailPayslipResponse emailPayslip(String email, Long payslipId, String targetEmail) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        Payslip ps = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found with ID: " + payslipId));

        if (!ps.getPayroll().getEmployee().getId().equals(emp.getId())) {
            throw new IllegalArgumentException("Access Denied: Payslip does not belong to you.");
        }

        Payroll pr = ps.getPayroll();
        String period = getMonthName(pr.getMonth()) + " " + pr.getYear();

        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;
                            padding: 32px; border: 1px solid #e5e7eb; border-radius: 10px; background: #fff;">
                    <h2 style="color: #1e293b; margin-bottom: 4px;">📄 Payslip Received</h2>
                    <p style="color: #64748b; margin-top: 0;">Hi %s,</p>
                    <p style="color: #64748b;">Please find the details for your payslip of <strong>%s</strong> below:</p>

                    <table style="width: 100%%; border-collapse: collapse; margin: 24px 0;">
                        <tr style="background: #f8fafc;">
                            <th style="padding: 12px; text-align: left; border-bottom: 1px solid #e2e8f0;">Details</th>
                            <th style="padding: 12px; text-align: right; border-bottom: 1px solid #e2e8f0;">Value</th>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #e2e8f0;">Payslip Number</td>
                            <td style="padding: 12px; text-align: right; border-bottom: 1px solid #e2e8f0; font-weight: bold;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #e2e8f0;">Basic Salary</td>
                            <td style="padding: 12px; text-align: right; border-bottom: 1px solid #e2e8f0;">INR %s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #e2e8f0;">Allowances</td>
                            <td style="padding: 12px; text-align: right; border-bottom: 1px solid #e2e8f0;">INR %s</td>
                        </tr>
                        <tr>
                            <td style="padding: 12px; border-bottom: 1px solid #e2e8f0;">Deductions</td>
                            <td style="padding: 12px; text-align: right; border-bottom: 1px solid #e2e8f0; color: #ef4444;">INR %s</td>
                        </tr>
                        <tr style="font-weight: bold; background: #f1f5f9;">
                            <td style="padding: 12px;">Net Payout</td>
                            <td style="padding: 12px; text-align: right; color: #10b981;">INR %s</td>
                        </tr>
                    </table>

                    <p style="color: #64748b; font-size: 14px;">
                        This is an automated copy of your payslip request. You can download the PDF statement anytime from your self-service dashboard.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;"/>
                    <p style="color: #94a3b8; font-size: 12px;">— EMS Payroll Team</p>
                </div>
                """.formatted(
                        emp.getFullName(),
                        period,
                        ps.getPayslipNumber(),
                        pr.getBasicSalary(),
                        pr.getAllowances(),
                        pr.getDeductions(),
                        pr.getNetPay()
                );

        try {
            emailService.sendEmail(targetEmail, "EMS Payslip Statement – " + period, html);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send payslip email: " + e.getMessage(), e);
        }

        return new EmailPayslipResponse("Payslip emailed successfully to " + targetEmail, payslipId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public PayrollTimelineResponse getPayrollTimeline(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found for email: " + email));

        List<Payroll> payrolls = payrollRepository.findByEmployeeId(emp.getId()).stream()
                .sorted(Comparator.comparing(Payroll::getYear).thenComparing(Payroll::getMonth))
                .collect(Collectors.toList());

        List<PayrollTimelineResponse.TimelineEventItem> events = new ArrayList<>();

        for (Payroll pr : payrolls) {
            LocalDate baseDate = pr.getGeneratedAt() != null ? pr.getGeneratedAt().toLocalDate() : LocalDate.of(pr.getYear(), pr.getMonth(), 1);
            events.add(new PayrollTimelineResponse.TimelineEventItem(
                    baseDate,
                    "GENERATED - Payroll generated for " + getMonthName(pr.getMonth()) + " " + pr.getYear(),
                    "System"
            ));

            if ("REVIEWED".equalsIgnoreCase(pr.getStatus()) || "APPROVED".equalsIgnoreCase(pr.getStatus())
                    || "PROCESSED".equalsIgnoreCase(pr.getStatus()) || "PAID".equalsIgnoreCase(pr.getStatus())) {
                LocalDate processedDate = pr.getProcessedAt() != null ? pr.getProcessedAt().toLocalDate() : baseDate.plusDays(2);
                events.add(new PayrollTimelineResponse.TimelineEventItem(
                        processedDate,
                        "PROCESSED - Payroll processed and statements compiled",
                        "Finance Department"
                ));
            }

            if ("PAID".equalsIgnoreCase(pr.getStatus())) {
                LocalDate paidDate = pr.getProcessedAt() != null ? pr.getProcessedAt().toLocalDate() : baseDate.plusDays(4);
                events.add(new PayrollTimelineResponse.TimelineEventItem(
                        paidDate,
                        "PAID - Salary credited to account",
                        "Bank Transfer (" + pr.getPaymentMode() + ")"
                ));
            }
        }

        return new PayrollTimelineResponse(events);
    }
}
