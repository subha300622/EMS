package com.example.ems.finance.service;

import com.example.ems.expense.entity.*;
import com.example.ems.expense.repository.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.finance.dto.*;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinanceExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private ExpenseAuditLogRepository expenseAuditLogRepository;

    @Autowired
    private MyExpenseReceiptRepository receiptRepository;

    private boolean isPending(ExpenseStatus status) {
        return status == ExpenseStatus.PENDING
                || status == ExpenseStatus.SUBMITTED
                || status == ExpenseStatus.PENDING_MANAGER_APPROVAL
                || status == ExpenseStatus.PENDING_FINANCE_APPROVAL;
    }

    private void validateTransition(ExpenseStatus cur, ExpenseStatus tgt) {
        if (cur == tgt) return;
        if (cur == ExpenseStatus.REJECTED && tgt == ExpenseStatus.APPROVED) {
            throw new IllegalStateException("REJECTED -> APPROVED is blocked.");
        }
        if (cur == ExpenseStatus.REIMBURSED && tgt == ExpenseStatus.APPROVED) {
            throw new IllegalStateException("REIMBURSED -> APPROVED is blocked.");
        }
        if (cur == ExpenseStatus.APPROVED && tgt == ExpenseStatus.REJECTED) {
            throw new IllegalStateException("APPROVED -> REJECTED is blocked.");
        }
        if (cur == ExpenseStatus.REIMBURSED && tgt == ExpenseStatus.SENT_BACK) {
            throw new IllegalStateException("REIMBURSED -> SENT_BACK is blocked.");
        }
    }

    // ── 1. DASHBOARD APIs ─────────────────────────────────────────────────────
    public ExpenseDashboardResponse getDashboard() {
        List<Expense> allExpenses = expenseRepository.findAll();

        long totalPending = 0;
        BigDecimal pendingAmount = BigDecimal.ZERO;
        long approvedThisMonth = 0;
        BigDecimal approvedAmountThisMonth = BigDecimal.ZERO;
        long rejected = 0;
        BigDecimal rejectedAmount = BigDecimal.ZERO;

        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        double totalDays = 0;
        long approvedCount = 0;

        for (Expense e : allExpenses) {
            ExpenseStatus status = e.getExpenseStatus();
            BigDecimal amount = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;

            if (isPending(status)) {
                totalPending++;
                pendingAmount = pendingAmount.add(amount);
            } else if (status == ExpenseStatus.REJECTED) {
                rejected++;
                rejectedAmount = rejectedAmount.add(amount);
            } else if (status == ExpenseStatus.APPROVED || status == ExpenseStatus.REIMBURSED) {
                LocalDateTime sub = e.getSubmittedAt();
                LocalDateTime app = e.getUpdatedAt();

                // Check audit log for when it was first approved
                List<ExpenseAuditLog> logs = expenseAuditLogRepository.findByExpenseIdOrderByUpdatedAtAsc(e.getId());
                for (ExpenseAuditLog log : logs) {
                    if (log.getStatus() == ExpenseStatus.APPROVED) {
                        app = log.getUpdatedAt();
                        break;
                    }
                }

                if (app != null && app.getMonthValue() == currentMonth && app.getYear() == currentYear) {
                    approvedThisMonth++;
                    approvedAmountThisMonth = approvedAmountThisMonth.add(amount);
                }

                if (sub != null && app != null) {
                    long diffMs = java.time.Duration.between(sub, app).toMillis();
                    double diffDays = (double) diffMs / (1000.0 * 60 * 60 * 24);
                    if (diffDays < 0) diffDays = 0;
                    totalDays += diffDays;
                    approvedCount++;
                }
            }
        }

        double averageApprovalDays = approvedCount > 0 ? (double) Math.round((totalDays / approvedCount) * 10) / 10 : 0.0;

        return new ExpenseDashboardResponse(
                totalPending,
                pendingAmount,
                approvedThisMonth,
                approvedAmountThisMonth,
                rejected,
                rejectedAmount,
                averageApprovalDays
        );
    }

    // ── 2. EXPENSE LISTING ───────────────────────────────────────────────────
    public FinanceExpenseListResponse getExpenses(String status, String category, String department, String search,
                                                BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable) {
        List<Expense> filtered = filterExpenses(status, category, department, search, minAmount, maxAmount, null, null);

        long totalElements = filtered.size();
        int size = pageable.getPageSize();
        int page = pageable.getPageNumber();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int start = page * size;
        int end = Math.min(start + size, filtered.size());
        List<FinanceExpenseListItem> content = new ArrayList<>();

        if (start < filtered.size()) {
            content = filtered.subList(start, end).stream()
                    .map(e -> {
                        Employee emp = e.getEmployee();
                        LocalDate submittedDate = e.getSubmittedAt() != null ? e.getSubmittedAt().toLocalDate() : e.getExpenseDate();
                        boolean receiptAttached = (e.getAttachmentUrl() != null && !e.getAttachmentUrl().isBlank())
                                || !e.getReceipts().isEmpty();

                        return new FinanceExpenseListItem(
                                e.getId(),
                                emp != null ? emp.getId() : null,
                                emp != null ? emp.getFullName() : null,
                                emp != null ? emp.getDepartment() : null,
                                e.getCategory() != null ? e.getCategory().getCode() : null,
                                e.getDescription(),
                                e.getAmount(),
                                receiptAttached,
                                submittedDate,
                                mapToStandardStatus(e.getExpenseStatus())
                        );
                    })
                    .collect(Collectors.toList());
        }

        return new FinanceExpenseListResponse(content, totalElements, totalPages);
    }

    private String mapToStandardStatus(ExpenseStatus s) {
        if (s == null) return "PENDING";
        if (isPending(s)) return "PENDING";
        return s.name();
    }

    // ── 3. EXPENSE DETAILS ───────────────────────────────────────────────────
    public FinanceExpenseDetailsResponse getExpenseDetails(Long expenseId) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        Employee emp = e.getEmployee();
        FinanceExpenseDetailsResponse.EmployeeInfo empInfo = null;
        if (emp != null) {
            empInfo = new FinanceExpenseDetailsResponse.EmployeeInfo(emp.getId(), emp.getFullName(), emp.getDepartment());
        }

        LocalDate submittedDate = e.getSubmittedAt() != null ? e.getSubmittedAt().toLocalDate() : e.getExpenseDate();
        boolean receiptAttached = (e.getAttachmentUrl() != null && !e.getAttachmentUrl().isBlank())
                                || !e.getReceipts().isEmpty();

        String receiptUrl = e.getAttachmentUrl();
        if ((receiptUrl == null || receiptUrl.isBlank()) && !e.getReceipts().isEmpty()) {
            receiptUrl = "/api/v1/files/receipts/receipt-" + expenseId + ".pdf";
        }

        return new FinanceExpenseDetailsResponse(
                e.getId(),
                empInfo,
                e.getCategory() != null ? e.getCategory().getCode() : null,
                e.getDescription(),
                e.getAmount(),
                e.getBusinessPurpose() != null ? e.getBusinessPurpose() : e.getTitle(),
                submittedDate,
                mapToStandardStatus(e.getExpenseStatus()),
                receiptAttached,
                receiptUrl
        );
    }

    // ── 4. RECEIPT APIs ──────────────────────────────────────────────────────
    public FinanceExpenseReceiptResponse getReceiptMetadata(Long expenseId) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        String fileName = "receipt-" + expenseId + ".pdf";
        String contentType = "application/pdf";
        long size = 10000L;

        if (e.getAttachmentName() != null && !e.getAttachmentName().isBlank()) {
            fileName = e.getAttachmentName();
            contentType = e.getAttachmentType() != null ? e.getAttachmentType() : "application/pdf";
            size = e.getAttachmentData() != null ? e.getAttachmentData().length : 10000L;
        } else if (!e.getReceipts().isEmpty()) {
            fileName = e.getReceipts().get(0).getFileName();
            contentType = e.getReceipts().get(0).getFileType() != null ? e.getReceipts().get(0).getFileType() : "application/pdf";
            size = e.getReceipts().get(0).getFileSize();
        }

        String downloadUrl = "/api/v1/files/receipts/receipt-" + expenseId + ".pdf";
        return new FinanceExpenseReceiptResponse(fileName, contentType, size, downloadUrl);
    }

    // ── 5. APPROVE CLAIM ─────────────────────────────────────────────────────
    @Transactional
    public void approveExpense(Long expenseId, String remarks, String username) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        validateTransition(e.getExpenseStatus(), ExpenseStatus.APPROVED);

        e.setExpenseStatus(ExpenseStatus.APPROVED);
        e.setApprovalRemarks(remarks);
        e.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(e);

        expenseAuditLogRepository.save(new ExpenseAuditLog(expenseId, ExpenseStatus.APPROVED, remarks, username));
    }

    // ── 6. REJECT CLAIM ──────────────────────────────────────────────────────
    @Transactional
    public void rejectExpense(Long expenseId, String reason, String username) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        validateTransition(e.getExpenseStatus(), ExpenseStatus.REJECTED);

        e.setExpenseStatus(ExpenseStatus.REJECTED);
        e.setRejectionReason(reason);
        e.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(e);

        expenseAuditLogRepository.save(new ExpenseAuditLog(expenseId, ExpenseStatus.REJECTED, reason, username));
    }

    // ── 7. SEND BACK FOR CORRECTION ──────────────────────────────────────────
    @Transactional
    public void sendBackExpense(Long expenseId, String reason, String username) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        validateTransition(e.getExpenseStatus(), ExpenseStatus.SENT_BACK);

        e.setExpenseStatus(ExpenseStatus.SENT_BACK);
        e.setSendBackReason(reason);
        e.setUpdatedAt(LocalDateTime.now());
        expenseRepository.save(e);

        expenseAuditLogRepository.save(new ExpenseAuditLog(expenseId, ExpenseStatus.SENT_BACK, reason, username));
    }

    // ── 8. REIMBURSE CLAIM ───────────────────────────────────────────────────
    @Transactional
    public ReimburseExpenseResponse reimburseExpense(Long expenseId, String paymentMode, String txnRef, String username) {
        Expense e = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense claim not found with ID: " + expenseId));

        validateTransition(e.getExpenseStatus(), ExpenseStatus.REIMBURSED);

        e.setExpenseStatus(ExpenseStatus.REIMBURSED);
        e.setPaymentMode(paymentMode);
        e.setTransactionReference(txnRef);
        e.setUpdatedAt(LocalDateTime.now());
        Expense saved = expenseRepository.save(e);

        String remarks = "Payment Mode: " + paymentMode + ", Ref: " + txnRef;
        expenseAuditLogRepository.save(new ExpenseAuditLog(expenseId, ExpenseStatus.REIMBURSED, remarks, username));

        return new ReimburseExpenseResponse(saved.getId(), "REIMBURSED", paymentMode, txnRef);
    }

    // ── 9. BULK ACTIONS ──────────────────────────────────────────────────────
    @Transactional
    public void bulkApprove(List<Long> expenseIds, String remarks, String username) {
        for (Long id : expenseIds) {
            approveExpense(id, remarks, username);
        }
    }

    @Transactional
    public void bulkReject(List<Long> expenseIds, String remarks, String username) {
        for (Long id : expenseIds) {
            rejectExpense(id, remarks, username);
        }
    }

    // ── 10. TIMELINE APIs ─────────────────────────────────────────────────────
    public List<FinanceExpenseTimelineItem> getTimeline(Long expenseId) {
        if (!expenseRepository.existsById(expenseId)) {
            throw new IllegalArgumentException("Expense claim not found with ID: " + expenseId);
        }

        List<ExpenseAuditLog> logs = expenseAuditLogRepository.findByExpenseIdOrderByUpdatedAtAsc(expenseId);
        return logs.stream()
                .map(log -> new FinanceExpenseTimelineItem(
                        log.getStatus() != null ? log.getStatus().name() : "PENDING",
                        log.getUpdatedBy(),
                        log.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    // ── 11. REPORTS API (SUMMARY) ────────────────────────────────────────────
    public ExpenseReportsSummaryResponse getReportsSummary() {
        List<Expense> list = expenseRepository.findAll();

        long totalExpenses = list.size();
        long approvedExpenses = 0;
        long pendingExpenses = 0;
        long rejectedExpenses = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal approvedAmount = BigDecimal.ZERO;

        double totalDays = 0;
        long approvedCount = 0;

        for (Expense e : list) {
            BigDecimal amt = e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO;
            totalAmount = totalAmount.add(amt);

            ExpenseStatus status = e.getExpenseStatus();
            if (isPending(status)) {
                pendingExpenses++;
            } else if (status == ExpenseStatus.REJECTED) {
                rejectedExpenses++;
            } else if (status == ExpenseStatus.APPROVED || status == ExpenseStatus.REIMBURSED) {
                approvedExpenses++;
                approvedAmount = approvedAmount.add(amt);

                LocalDateTime sub = e.getSubmittedAt();
                LocalDateTime app = e.getUpdatedAt();

                List<ExpenseAuditLog> logs = expenseAuditLogRepository.findByExpenseIdOrderByUpdatedAtAsc(e.getId());
                for (ExpenseAuditLog log : logs) {
                    if (log.getStatus() == ExpenseStatus.APPROVED) {
                        app = log.getUpdatedAt();
                        break;
                    }
                }

                if (sub != null && app != null) {
                    long diffMs = java.time.Duration.between(sub, app).toMillis();
                    double diffDays = (double) diffMs / (1000.0 * 60 * 60 * 24);
                    if (diffDays < 0) diffDays = 0;
                    totalDays += diffDays;
                    approvedCount++;
                }
            }
        }

        double averageApprovalDays = approvedCount > 0 ? (double) Math.round((totalDays / approvedCount) * 10) / 10 : 0.0;

        return new ExpenseReportsSummaryResponse(
                totalExpenses,
                approvedExpenses,
                pendingExpenses,
                rejectedExpenses,
                totalAmount,
                approvedAmount,
                averageApprovalDays
        );
    }

    // ── 12. EXPORT APIs (CSV / PDF / XLSX) ────────────────────────────────────
    public List<Expense> filterExpenses(String status, String category, String department, String search,
                                        BigDecimal minAmount, BigDecimal maxAmount, LocalDate fromDate, LocalDate toDate) {
        List<Expense> list = expenseRepository.findAll();
        return list.stream()
                .filter(e -> {
                    // status filter
                    if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
                        ExpenseStatus filterStatus = null;
                        try {
                            if (!"PENDING".equalsIgnoreCase(status)) {
                                filterStatus = ExpenseStatus.valueOf(status.toUpperCase());
                            }
                        } catch (IllegalArgumentException ex) {
                            return false;
                        }

                        ExpenseStatus s = e.getExpenseStatus();
                        if ("PENDING".equalsIgnoreCase(status)) {
                            if (!isPending(s)) return false;
                        } else {
                            if (s != filterStatus) return false;
                        }
                    }
                    // category filter
                    if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
                        if (e.getCategory() == null || !category.equalsIgnoreCase(e.getCategory().getCode())) {
                            return false;
                        }
                    }
                    // department filter
                    if (department != null && !department.isBlank() && !"ALL".equalsIgnoreCase(department)) {
                        if (e.getEmployee() == null || !department.equalsIgnoreCase(e.getEmployee().getDepartment())) {
                            return false;
                        }
                    }
                    // search filter
                    if (search != null && !search.isBlank()) {
                        String term = search.toLowerCase();
                        boolean matchesName = e.getEmployee() != null && e.getEmployee().getFullName() != null
                                && e.getEmployee().getFullName().toLowerCase().contains(term);
                        boolean matchesDesc = e.getDescription() != null && e.getDescription().toLowerCase().contains(term);
                        boolean matchesPurpose = e.getBusinessPurpose() != null && e.getBusinessPurpose().toLowerCase().contains(term);
                        if (!matchesName && !matchesDesc && !matchesPurpose) {
                            return false;
                        }
                    }
                    // minAmount
                    if (minAmount != null) {
                        if (e.getAmount() == null || e.getAmount().compareTo(minAmount) < 0) {
                            return false;
                        }
                    }
                    // maxAmount
                    if (maxAmount != null) {
                        if (e.getAmount() == null || e.getAmount().compareTo(maxAmount) > 0) {
                            return false;
                        }
                    }
                    // date filters
                    LocalDate expDate = e.getExpenseDate();
                    if (fromDate != null && expDate != null && expDate.isBefore(fromDate)) {
                        return false;
                    }
                    if (toDate != null && expDate != null && expDate.isAfter(toDate)) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    public byte[] exportCsv(String status, String department, LocalDate fromDate, LocalDate toDate) {
        List<Expense> expenses = filterExpenses(status, null, department, null, null, null, fromDate, toDate);
        StringBuilder csv = new StringBuilder("Expense ID,Employee Name,Department,Category,Description,Amount,Date,Status\n");
        for (Expense e : expenses) {
            Employee emp = e.getEmployee();
            csv.append(e.getId()).append(",")
               .append(emp != null ? emp.getFullName().replace(",", " ") : "").append(",")
               .append(emp != null ? emp.getDepartment() : "").append(",")
               .append(e.getCategory() != null ? e.getCategory().getCode() : "").append(",")
               .append(e.getDescription() != null ? e.getDescription().replace(",", " ").replace("\n", " ") : "").append(",")
               .append(e.getAmount() != null ? e.getAmount() : "0.00").append(",")
               .append(e.getExpenseDate() != null ? e.getExpenseDate().toString() : "").append(",")
               .append(mapToStandardStatus(e.getExpenseStatus())).append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportXlsx(String status, String department, LocalDate fromDate, LocalDate toDate) {
        List<Expense> expenses = filterExpenses(status, null, department, null, null, null, fromDate, toDate);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Expense Approvals");

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Expense ID", "Employee Name", "Department", "Category", "Description", "Amount", "Date", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Data rows
            int rowIdx = 1;
            for (Expense e : expenses) {
                Row row = sheet.createRow(rowIdx++);
                Employee emp = e.getEmployee();

                row.createCell(0).setCellValue(e.getId());
                row.createCell(1).setCellValue(emp != null ? emp.getFullName() : "");
                row.createCell(2).setCellValue(emp != null ? emp.getDepartment() : "");
                row.createCell(3).setCellValue(e.getCategory() != null ? e.getCategory().getCode() : "");
                row.createCell(4).setCellValue(e.getDescription() != null ? e.getDescription() : "");
                row.createCell(5).setCellValue(e.getAmount() != null ? e.getAmount().doubleValue() : 0.0);
                row.createCell(6).setCellValue(e.getExpenseDate() != null ? e.getExpenseDate().toString() : "");
                row.createCell(7).setCellValue(mapToStandardStatus(e.getExpenseStatus()));
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error generating Excel report: " + ex.getMessage(), ex);
        }
    }

    public byte[] exportPdf(String status, String department, LocalDate fromDate, LocalDate toDate) {
        List<Expense> expenses = filterExpenses(status, null, department, null, null, null, fromDate, toDate);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            Paragraph title = new Paragraph("Expense Approvals Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingAfter(20);

            // Add Headers
            table.addCell(new Paragraph("ID", headerFont));
            table.addCell(new Paragraph("Employee", headerFont));
            table.addCell(new Paragraph("Department", headerFont));
            table.addCell(new Paragraph("Category", headerFont));
            table.addCell(new Paragraph("Amount", headerFont));
            table.addCell(new Paragraph("Date", headerFont));
            table.addCell(new Paragraph("Status", headerFont));

            for (Expense e : expenses) {
                Employee emp = e.getEmployee();
                table.addCell(new Paragraph(String.valueOf(e.getId()), normalFont));
                table.addCell(new Paragraph(emp != null ? emp.getFullName() : "", normalFont));
                table.addCell(new Paragraph(emp != null ? emp.getDepartment() : "", normalFont));
                table.addCell(new Paragraph(e.getCategory() != null ? e.getCategory().getCode() : "", normalFont));
                table.addCell(new Paragraph(e.getAmount() != null ? e.getAmount().toString() : "0.00", normalFont));
                table.addCell(new Paragraph(e.getExpenseDate() != null ? e.getExpenseDate().toString() : "", normalFont));
                table.addCell(new Paragraph(mapToStandardStatus(e.getExpenseStatus()), normalFont));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error generating PDF: " + ex.getMessage(), ex);
        }
    }
}
