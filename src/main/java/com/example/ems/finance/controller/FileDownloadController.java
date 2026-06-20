package com.example.ems.finance.controller;

import com.example.ems.finance.service.FinanceAssetCostReportService;
import com.example.ems.finance.service.FinanceSettlementService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.payroll.repository.FnfSettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin("*")
public class FileDownloadController {

    @Autowired
    private FinanceAssetCostReportService reportService;

    @Autowired
    private FinanceSettlementService settlementService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private FnfSettlementRepository settlementRepository;

    @Autowired
    private com.example.ems.expense.repository.ExpenseRepository expenseRepository;

    // ── Receipt folders download subpath ──────────────────────────────────────
    @GetMapping("/receipts/{fileName:.+}")
    public ResponseEntity<?> downloadReceiptFolderFile(@PathVariable("fileName") String fileName) {
        return serveReceiptFile(fileName);
    }

    private ResponseEntity<?> serveReceiptFile(String fileName) {
        if (fileName.startsWith("receipt-") && fileName.endsWith(".pdf")) {
            String idStr = fileName.substring(8, fileName.length() - 4);
            try {
                Long expenseId = Long.parseLong(idStr);
                Optional<com.example.ems.expense.entity.Expense> expOpt = expenseRepository.findById(expenseId);
                if (expOpt.isPresent()) {
                    com.example.ems.expense.entity.Expense e = expOpt.get();
                    if (e.getAttachmentData() != null && e.getAttachmentData().length > 0) {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType(e.getAttachmentType() != null ? e.getAttachmentType() : "application/pdf"));
                        headers.setContentDispositionFormData("attachment", e.getAttachmentName() != null ? e.getAttachmentName() : fileName);
                        headers.setContentLength(e.getAttachmentData().length);
                        return new ResponseEntity<>(e.getAttachmentData(), headers, HttpStatus.OK);
                    } else if (!e.getReceipts().isEmpty()) {
                        com.example.ems.expense.entity.MyExpenseReceipt r = e.getReceipts().get(0);
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.parseMediaType(r.getFileType() != null ? r.getFileType() : "application/pdf"));
                        headers.setContentDispositionFormData("attachment", r.getFileName());
                        headers.setContentLength(r.getFileData().length);
                        return new ResponseEntity<>(r.getFileData(), headers, HttpStatus.OK);
                    }
                }
            } catch (NumberFormatException ex) {
                // ignore
            }
        }
        return ResponseEntity.notFound().build();
    }

    // ── F&F Statements and general files download path ────────────────────────
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<?> downloadGeneralFile(@PathVariable("fileName") String fileName) {
        // Receipt download direct fallback
        if (fileName.startsWith("receipt-") && fileName.endsWith(".pdf")) {
            return serveReceiptFile(fileName);
        }

        // Asset cost report downloads
        if (fileName.startsWith("asset-cost-report-")) {
            if (fileName.endsWith(".csv")) {
                byte[] data = reportService.generateCsvExportBytes();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType("text/csv"));
                headers.setContentDispositionFormData("attachment", fileName);
                headers.setContentLength(data.length);
                return new ResponseEntity<>(data, headers, HttpStatus.OK);
            } else if (fileName.endsWith(".pdf")) {
                byte[] data = reportService.generatePdfExportBytes();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", fileName);
                headers.setContentLength(data.length);
                return new ResponseEntity<>(data, headers, HttpStatus.OK);
            }
        }

        // Settlements export
        if ("settlements-export.csv".equalsIgnoreCase(fileName)) {
            byte[] data = settlementService.exportSettlements();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(data.length);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        }

        // FNF Statement PDF
        if (fileName.startsWith("FNF-") && fileName.endsWith(".pdf")) {
            String name = fileName.substring(4, fileName.length() - 4).replace("-", " ");
            Optional<Employee> empOpt = employeeRepository.findAll().stream()
                    .filter(e -> e.getFullName().replace(" ", "").equalsIgnoreCase(name.replace(" ", "")))
                    .findFirst();
            if (empOpt.isPresent()) {
                Optional<FnfSettlement> settOpt = settlementRepository.findByEmployeeId(empOpt.get().getId()).stream().findFirst();
                if (settOpt.isPresent()) {
                    byte[] data = settlementService.generateFnfPdfFileBytes(settOpt.get().getId());
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", fileName);
                    headers.setContentLength(data.length);
                    return new ResponseEntity<>(data, headers, HttpStatus.OK);
                }
            }
        }

        return ResponseEntity.notFound().build();
    }
}
