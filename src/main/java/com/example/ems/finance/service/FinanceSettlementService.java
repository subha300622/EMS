package com.example.ems.finance.service;

import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.FnfSettlementRepository;
import com.example.ems.payroll.repository.FnfSettlementAuditRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.finance.dto.*;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FinanceSettlementService {

    @Autowired
    private FnfSettlementRepository settlementRepository;

    @Autowired
    private FnfSettlementAuditRepository auditRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private MyAssetRepository assetRepository;

    // Helper: Map DB Status to API Status String
    private String mapStatusToApi(FnfSettlementStatus status) {
        if (status == null) return "PENDING_REVIEW";
        switch (status) {
            case PENDING: return "PENDING_REVIEW";
            case SENT_BACK: return "SENT_BACK";
            case APPROVED: return "APPROVED";
            case PROCESSED: return "PROCESSED";
            case REJECTED: return "REJECTED";
            default: return "PENDING_REVIEW";
        }
    }

    // Helper: Map API Status String to DB Status Enum
    private FnfSettlementStatus mapStatusToDb(String statusStr) {
        if (statusStr == null) return null;
        switch (statusStr.trim().toUpperCase()) {
            case "PENDING_REVIEW": return FnfSettlementStatus.PENDING;
            case "SENT_BACK": return FnfSettlementStatus.SENT_BACK;
            case "APPROVED": return FnfSettlementStatus.APPROVED;
            case "PROCESSED": return FnfSettlementStatus.PROCESSED;
            case "REJECTED": return FnfSettlementStatus.REJECTED;
            default: return null;
        }
    }

    // ── 1. GET DASHBOARD ──────────────────────────────────────────────────────
    public SettlementDashboardResponse getDashboard() {
        List<FnfSettlement> list = settlementRepository.findAll();

        long pendingReview = 0;
        long approved = 0;
        long processed = 0;
        long rejected = 0;
        BigDecimal totalSettlementAmount = BigDecimal.ZERO;
        long nonRejectedCount = 0;
        BigDecimal pendingDisbursementAmount = BigDecimal.ZERO;

        for (FnfSettlement s : list) {
            if (s.getStatus() == FnfSettlementStatus.PENDING) {
                pendingReview++;
            } else if (s.getStatus() == FnfSettlementStatus.APPROVED) {
                approved++;
                if (s.getNetAmount() != null) {
                    pendingDisbursementAmount = pendingDisbursementAmount.add(s.getNetAmount());
                }
            } else if (s.getStatus() == FnfSettlementStatus.PROCESSED) {
                processed++;
            } else if (s.getStatus() == FnfSettlementStatus.REJECTED) {
                rejected++;
            }

            if (s.getStatus() != FnfSettlementStatus.REJECTED) {
                nonRejectedCount++;
                if (s.getNetAmount() != null) {
                    totalSettlementAmount = totalSettlementAmount.add(s.getNetAmount());
                }
            }
        }

        BigDecimal avgSettlementAmount = BigDecimal.ZERO;
        if (nonRejectedCount > 0) {
            avgSettlementAmount = totalSettlementAmount.divide(BigDecimal.valueOf(nonRejectedCount), 2, RoundingMode.HALF_UP);
        }

        return new SettlementDashboardResponse(
                pendingReview,
                approved,
                processed,
                rejected,
                totalSettlementAmount,
                avgSettlementAmount,
                pendingDisbursementAmount
        );
    }

    // ── 2. GET SETTLEMENTS (LIST WITH FILTERS & PAGINATION) ───────────────────
    public SettlementListResponse getSettlements(String statusStr, String search, String department, int page, int size) {
        List<FnfSettlement> allSettlements = settlementRepository.findAll();
        List<SettlementListItem> enrichedItems = new ArrayList<>();

        FnfSettlementStatus filterStatus = mapStatusToDb(statusStr);

        for (FnfSettlement s : allSettlements) {
            // Filter by status if provided
            if (filterStatus != null && s.getStatus() != filterStatus) {
                continue;
            }

            Employee emp = employeeRepository.findById(s.getEmployeeId()).orElse(null);
            if (emp == null) {
                continue;
            }

            // Filter by department
            if (department != null && !department.isBlank() && !department.equalsIgnoreCase(emp.getDepartment())) {
                continue;
            }

            // Filter by search (name, email, department, or employeeId)
            if (search != null && !search.isBlank()) {
                String term = search.toLowerCase();
                boolean matchesName = emp.getFullName() != null && emp.getFullName().toLowerCase().contains(term);
                boolean matchesEmail = emp.getEmail() != null && emp.getEmail().toLowerCase().contains(term);
                boolean matchesDept = emp.getDepartment() != null && emp.getDepartment().toLowerCase().contains(term);
                boolean matchesEmpId = emp.getEmployeeId() != null && emp.getEmployeeId().toLowerCase().contains(term);
                if (!matchesName && !matchesEmail && !matchesDept && !matchesEmpId) {
                    continue;
                }
            }

            LocalDate lastWorkingDate = null;
            Optional<Offboarding> offboardingOpt = offboardingRepository.findByEmployeeId(emp.getId());
            if (offboardingOpt.isPresent()) {
                lastWorkingDate = offboardingOpt.get().getExitDate();
                if (lastWorkingDate == null) {
                    lastWorkingDate = offboardingOpt.get().getRequestedLastWorkingDay();
                }
            }

            BigDecimal grossAmount = (s.getGratuity() != null ? s.getGratuity() : BigDecimal.ZERO)
                    .add(s.getNoticePay() != null ? s.getNoticePay() : BigDecimal.ZERO)
                    .add(s.getUnpaidSalary() != null ? s.getUnpaidSalary() : BigDecimal.ZERO);

            BigDecimal deductionAmount = s.getOtherDeductions() != null ? s.getOtherDeductions() : BigDecimal.ZERO;
            BigDecimal netAmount = s.getNetAmount() != null ? s.getNetAmount() : BigDecimal.ZERO;

            enrichedItems.add(new SettlementListItem(
                    s.getId(),
                    emp.getId(),
                    emp.getFullName(),
                    emp.getDepartment(),
                    lastWorkingDate,
                    grossAmount,
                    deductionAmount,
                    netAmount,
                    mapStatusToApi(s.getStatus())
            ));
        }

        // Pagination
        int totalElements = enrichedItems.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) {
            totalPages = 1;
        }

        int start = page * size;
        int end = Math.min(start + size, totalElements);
        List<SettlementListItem> pagedContent = new ArrayList<>();
        if (start < totalElements) {
            pagedContent = enrichedItems.subList(start, end);
        }

        return new SettlementListResponse(pagedContent, page, size, totalElements, totalPages);
    }

    // ── 3. GET REVIEW POPUP ───────────────────────────────────────────────────
    public SettlementReviewResponse getReviewPopup(Long settlementId) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        Employee emp = employeeRepository.findById(s.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + s.getEmployeeId()));

        List<LineItem> earnings = new ArrayList<>();
        if (s.getUnpaidSalary() != null && s.getUnpaidSalary().compareTo(BigDecimal.ZERO) > 0) {
            earnings.add(new LineItem("Last Working Month Salary", s.getUnpaidSalary()));
        }
        if (s.getGratuity() != null && s.getGratuity().compareTo(BigDecimal.ZERO) > 0) {
            earnings.add(new LineItem("Gratuity", s.getGratuity()));
        }
        if (s.getNoticePay() != null && s.getNoticePay().compareTo(BigDecimal.ZERO) > 0) {
            earnings.add(new LineItem("Leave Encashment", s.getNoticePay()));
        }

        // Deductions calculation
        List<LineItem> deductions = new ArrayList<>();
        List<MyAsset> assignedAssets = assetRepository.findByAssignedToIdAndStatus(emp.getId(), "ASSIGNED");
        BigDecimal assetLoss = BigDecimal.ZERO;
        for (MyAsset a : assignedAssets) {
            BigDecimal recovery = a.getAssetRecoveryAmount();
            if (recovery != null) {
                assetLoss = assetLoss.add(recovery);
            }
        }

        BigDecimal otherDeductions = s.getOtherDeductions() != null ? s.getOtherDeductions() : BigDecimal.ZERO;
        BigDecimal leaveAdjustment = otherDeductions.subtract(assetLoss);
        if (leaveAdjustment.compareTo(BigDecimal.ZERO) < 0) {
            assetLoss = otherDeductions;
            leaveAdjustment = BigDecimal.ZERO;
        }

        if (assetLoss.compareTo(BigDecimal.ZERO) > 0) {
            deductions.add(new LineItem("Asset Recovery", assetLoss));
        }
        if (leaveAdjustment.compareTo(BigDecimal.ZERO) > 0) {
            deductions.add(new LineItem("Leave Adjustment / Other Deductions", leaveAdjustment));
        }

        BigDecimal grossAmount = (s.getGratuity() != null ? s.getGratuity() : BigDecimal.ZERO)
                .add(s.getNoticePay() != null ? s.getNoticePay() : BigDecimal.ZERO)
                .add(s.getUnpaidSalary() != null ? s.getUnpaidSalary() : BigDecimal.ZERO);

        SettlementReviewResponse.EmployeeInfo empInfo = new SettlementReviewResponse.EmployeeInfo(
                emp.getId(),
                emp.getFullName(),
                emp.getDepartment()
        );

        return new SettlementReviewResponse(
                s.getId(),
                empInfo,
                earnings,
                deductions,
                grossAmount,
                otherDeductions,
                s.getNetAmount(),
                mapStatusToApi(s.getStatus())
        );
    }

    // ── 4. GET ASSET CLEARANCE ────────────────────────────────────────────────
    public AssetRecoveryResponse getAssetClearance(Long settlementId) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        Employee emp = employeeRepository.findById(s.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + s.getEmployeeId()));

        List<MyAsset> allEmpAssets = assetRepository.findByAssignedToId(emp.getId());
        List<AssetRecoveryItem> items = new ArrayList<>();

        int returnedAssets = 0;
        int pendingAssets = 0;
        BigDecimal assetDeduction = BigDecimal.ZERO;

        for (MyAsset a : allEmpAssets) {
            boolean isReturned = "RETURNED".equalsIgnoreCase(a.getStatus());
            String apiStatus = isReturned ? "RETURNED" : "NOT_RETURNED";
            BigDecimal recoveryVal = a.getAssetRecoveryAmount() != null ? a.getAssetRecoveryAmount() : BigDecimal.ZERO;

            BigDecimal deduction = isReturned ? BigDecimal.ZERO : recoveryVal;

            if (isReturned) {
                returnedAssets++;
            } else {
                pendingAssets++;
                assetDeduction = assetDeduction.add(deduction);
            }

            items.add(new AssetRecoveryItem(
                    a.getId(),
                    a.getAssetName(),
                    apiStatus,
                    deduction
            ));
        }

        return new AssetRecoveryResponse(
                emp.getId(),
                returnedAssets,
                pendingAssets,
                assetDeduction,
                items
        );
    }

    // ── 5. GET TIMELINE ───────────────────────────────────────────────────────
    public List<SettlementTimelineItem> getTimeline(Long settlementId) {
        if (!settlementRepository.existsById(settlementId)) {
            throw new IllegalArgumentException("Settlement not found with ID: " + settlementId);
        }

        List<FnfSettlementAudit> auditTrail = auditRepository.findBySettlementIdOrderByUpdatedAtAsc(settlementId);
        return auditTrail.stream()
                .map(a -> new SettlementTimelineItem(
                        mapStatusToApi(a.getStatus()),
                        a.getUpdatedBy(),
                        a.getUpdatedAt(),
                        a.getRemarks()
                ))
                .collect(Collectors.toList());
    }

    // ── 6. SEND BACK TO HR ─────────────────────────────────────────────────────
    @Transactional
    public void sendBack(Long settlementId, String reason, String username) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        s.setStatus(FnfSettlementStatus.SENT_BACK);
        s.setWorkflowRemarks(reason);
        s.setUpdatedAt(LocalDateTime.now());
        settlementRepository.save(s);

        auditRepository.save(new FnfSettlementAudit(
                settlementId,
                FnfSettlementStatus.SENT_BACK,
                username,
                reason
        ));
    }

    // ── 7. REJECT SETTLEMENT ──────────────────────────────────────────────────
    @Transactional
    public void reject(Long settlementId, String reason, String username) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        s.setStatus(FnfSettlementStatus.REJECTED);
        s.setWorkflowRemarks(reason);
        s.setUpdatedAt(LocalDateTime.now());
        settlementRepository.save(s);

        auditRepository.save(new FnfSettlementAudit(
                settlementId,
                FnfSettlementStatus.REJECTED,
                username,
                reason
        ));
    }

    // ── 8. APPROVE SETTLEMENT ─────────────────────────────────────────────────
    @Transactional
    public FnfSettlement approve(Long settlementId, String remarks, String username) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        s.setStatus(FnfSettlementStatus.APPROVED);
        s.setWorkflowRemarks(remarks);
        s.setApprovedBy(username);
        s.setApprovedDate(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        FnfSettlement saved = settlementRepository.save(s);

        auditRepository.save(new FnfSettlementAudit(
                settlementId,
                FnfSettlementStatus.APPROVED,
                username,
                remarks
        ));

        return saved;
    }

    // ── 9. PROCESS SETTLEMENT ─────────────────────────────────────────────────
    @Transactional
    public FnfSettlement process(Long settlementId, PaymentMode paymentMode, String txRef, String username) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        s.setStatus(FnfSettlementStatus.PROCESSED);
        s.setPaymentMode(paymentMode);
        s.setPaymentReference(txRef);
        s.setProcessedBy(username);
        s.setProcessedDate(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        FnfSettlement saved = settlementRepository.save(s);

        auditRepository.save(new FnfSettlementAudit(
                settlementId,
                FnfSettlementStatus.PROCESSED,
                username,
                "Payment mode: " + paymentMode + ", Reference: " + txRef
        ));

        return saved;
    }

    // ── 10. GET STATUS DETAILS ────────────────────────────────────────────────
    public SettlementStatusResponse getStatusDetails(Long settlementId) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        boolean ready = s.getStatus() == FnfSettlementStatus.APPROVED || s.getStatus() == FnfSettlementStatus.PROCESSED;

        return new SettlementStatusResponse(
                s.getId(),
                mapStatusToApi(s.getStatus()),
                ready,
                s.getApprovedBy(),
                s.getApprovedDate()
        );
    }

    // ── 11. GET PDF METADATA ──────────────────────────────────────────────────
    public SettlementPdfResponse getPdfMetadata(Long settlementId) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        Employee emp = employeeRepository.findById(s.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found for settlement."));

        String sanitized = emp.getFullName().replace(" ", "-");
        String fileName = "FNF-" + sanitized + ".pdf";
        String downloadUrl = "/api/v1/files/" + fileName;

        return new SettlementPdfResponse(fileName, downloadUrl);
    }

    // ── 12. GENERATE PDF FILE BYTES (using OpenPDF) ───────────────────────────
    public byte[] generateFnfPdfFileBytes(Long settlementId) {
        FnfSettlement s = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found with ID: " + settlementId));

        Employee emp = employeeRepository.findById(s.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found for settlement."));

        LocalDate lastWorkingDate = null;
        Optional<Offboarding> offboardingOpt = offboardingRepository.findByEmployeeId(emp.getId());
        if (offboardingOpt.isPresent()) {
            lastWorkingDate = offboardingOpt.get().getExitDate();
            if (lastWorkingDate == null) {
                lastWorkingDate = offboardingOpt.get().getRequestedLastWorkingDay();
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Font styles
            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font boldFont = new Font(Font.HELVETICA, 11, Font.BOLD);

            // Document Title
            Paragraph title = new Paragraph("Full & Final Settlement Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Employee details table
            Paragraph empSection = new Paragraph("Employee Details", sectionFont);
            empSection.setSpacingAfter(10);
            document.add(empSection);

            PdfPTable empTable = new PdfPTable(2);
            empTable.setWidthPercentage(100);
            empTable.setSpacingAfter(20);

            empTable.addCell(new Paragraph("Employee Name:", boldFont));
            empTable.addCell(new Paragraph(emp.getFullName(), normalFont));

            empTable.addCell(new Paragraph("Employee ID:", boldFont));
            empTable.addCell(new Paragraph(emp.getEmployeeId(), normalFont));

            empTable.addCell(new Paragraph("Department:", boldFont));
            empTable.addCell(new Paragraph(emp.getDepartment(), normalFont));

            empTable.addCell(new Paragraph("Designation:", boldFont));
            empTable.addCell(new Paragraph(emp.getDesignation(), normalFont));

            empTable.addCell(new Paragraph("Last Working Date:", boldFont));
            empTable.addCell(new Paragraph(lastWorkingDate != null ? lastWorkingDate.toString() : "N/A", normalFont));

            document.add(empTable);

            // Calculations / Line Items
            Paragraph calcSection = new Paragraph("Financial Breakdown", sectionFont);
            calcSection.setSpacingAfter(10);
            document.add(calcSection);

            PdfPTable financialTable = new PdfPTable(2);
            financialTable.setWidthPercentage(100);
            financialTable.setSpacingAfter(20);

            financialTable.addCell(new Paragraph("Earnings Item", boldFont));
            financialTable.addCell(new Paragraph("Amount (INR)", boldFont));

            BigDecimal gross = BigDecimal.ZERO;
            if (s.getUnpaidSalary() != null) {
                financialTable.addCell(new Paragraph("Last Working Month Salary", normalFont));
                financialTable.addCell(new Paragraph(s.getUnpaidSalary().toString(), normalFont));
                gross = gross.add(s.getUnpaidSalary());
            }
            if (s.getGratuity() != null) {
                financialTable.addCell(new Paragraph("Gratuity", normalFont));
                financialTable.addCell(new Paragraph(s.getGratuity().toString(), normalFont));
                gross = gross.add(s.getGratuity());
            }
            if (s.getNoticePay() != null) {
                financialTable.addCell(new Paragraph("Leave Encashment", normalFont));
                financialTable.addCell(new Paragraph(s.getNoticePay().toString(), normalFont));
                gross = gross.add(s.getNoticePay());
            }

            financialTable.addCell(new Paragraph("Gross Earnings:", boldFont));
            financialTable.addCell(new Paragraph(gross.toString(), boldFont));

            // Deductions
            financialTable.addCell(new Paragraph("Deductions Item", boldFont));
            financialTable.addCell(new Paragraph("Amount (INR)", boldFont));

            BigDecimal deductionsVal = s.getOtherDeductions() != null ? s.getOtherDeductions() : BigDecimal.ZERO;
            financialTable.addCell(new Paragraph("Other Deductions / Asset Recovery", normalFont));
            financialTable.addCell(new Paragraph(deductionsVal.toString(), normalFont));

            financialTable.addCell(new Paragraph("Total Deductions:", boldFont));
            financialTable.addCell(new Paragraph(deductionsVal.toString(), boldFont));

            // Net
            financialTable.addCell(new Paragraph("Net Amount Payable:", boldFont));
            financialTable.addCell(new Paragraph(s.getNetAmount() != null ? s.getNetAmount().toString() : "0.00", boldFont));

            document.add(financialTable);

            // Workflow/Signature placeholder
            Paragraph sigSection = new Paragraph("Verification & Status", sectionFont);
            sigSection.setSpacingAfter(10);
            document.add(sigSection);

            PdfPTable sigTable = new PdfPTable(2);
            sigTable.setWidthPercentage(100);

            sigTable.addCell(new Paragraph("Settlement Status:", boldFont));
            sigTable.addCell(new Paragraph(mapStatusToApi(s.getStatus()), normalFont));

            sigTable.addCell(new Paragraph("Approved By:", boldFont));
            sigTable.addCell(new Paragraph(s.getApprovedBy() != null ? s.getApprovedBy() : "N/A", normalFont));

            sigTable.addCell(new Paragraph("Processed Date:", boldFont));
            sigTable.addCell(new Paragraph(s.getProcessedDate() != null ? s.getProcessedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "N/A", normalFont));

            document.add(sigTable);

            document.close();
            return baos.toByteArray();
        } catch (com.lowagie.text.DocumentException | java.io.IOException e) {
            throw new RuntimeException("Error during FNF PDF statement generation: " + e.getMessage(), e);
        }
    }

    // ── 13. GET REPORTS SUMMARY ───────────────────────────────────────────────
    public SettlementReportsSummaryResponse getReportsSummary() {
        List<FnfSettlement> list = settlementRepository.findAll();

        long totalProcessed = 0;
        BigDecimal totalAmountPaid = BigDecimal.ZERO;
        long pendingCases = 0;

        for (FnfSettlement s : list) {
            if (s.getStatus() == FnfSettlementStatus.PROCESSED) {
                totalProcessed++;
                if (s.getNetAmount() != null) {
                    totalAmountPaid = totalAmountPaid.add(s.getNetAmount());
                }
            } else if (s.getStatus() == FnfSettlementStatus.PENDING || s.getStatus() == FnfSettlementStatus.APPROVED) {
                pendingCases++;
            }
        }

        BigDecimal averageSettlement = BigDecimal.ZERO;
        if (totalProcessed > 0) {
            averageSettlement = totalAmountPaid.divide(BigDecimal.valueOf(totalProcessed), 2, RoundingMode.HALF_UP);
        }

        return new SettlementReportsSummaryResponse(
                totalProcessed,
                totalAmountPaid,
                averageSettlement,
                pendingCases
        );
    }

    // ── 14. EXPORT SETTLEMENTS CSV ────────────────────────────────────────────
    public byte[] exportSettlements() {
        List<FnfSettlement> list = settlementRepository.findAll();
        StringBuilder csv = new StringBuilder("Settlement ID,Employee ID,Employee Name,Department,Exit Date,Gross Earnings,Deductions,Net Settlement,Status\n");

        for (FnfSettlement s : list) {
            Employee emp = employeeRepository.findById(s.getEmployeeId()).orElse(null);
            if (emp == null) continue;

            LocalDate lastWorkingDate = null;
            Optional<Offboarding> offboardingOpt = offboardingRepository.findByEmployeeId(emp.getId());
            if (offboardingOpt.isPresent()) {
                lastWorkingDate = offboardingOpt.get().getExitDate();
                if (lastWorkingDate == null) {
                    lastWorkingDate = offboardingOpt.get().getRequestedLastWorkingDay();
                }
            }

            BigDecimal gross = (s.getGratuity() != null ? s.getGratuity() : BigDecimal.ZERO)
                    .add(s.getNoticePay() != null ? s.getNoticePay() : BigDecimal.ZERO)
                    .add(s.getUnpaidSalary() != null ? s.getUnpaidSalary() : BigDecimal.ZERO);

            csv.append(s.getId()).append(",")
               .append(emp.getId()).append(",")
               .append(emp.getFullName().replace(",", " ")).append(",")
               .append(emp.getDepartment()).append(",")
               .append(lastWorkingDate != null ? lastWorkingDate.toString() : "").append(",")
               .append(gross).append(",")
               .append(s.getOtherDeductions() != null ? s.getOtherDeductions() : "0.00").append(",")
               .append(s.getNetAmount() != null ? s.getNetAmount() : "0.00").append(",")
               .append(mapStatusToApi(s.getStatus())).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // Custom helper method for seeder to log audit entry
    @Transactional
    public void logAudit(Long settlementId, FnfSettlementStatus status, String username, String remarks) {
        auditRepository.save(new FnfSettlementAudit(settlementId, status, username, remarks));
    }
}
