package com.example.ems.finance.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.finance.dto.*;
import com.example.ems.finance.service.FinanceExpenseService;
import com.example.ems.security.service.JwtService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FinanceExpenseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FinanceExpenseService financeExpenseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FinanceExpenseController controller;

    private static final String TOKEN = "mock-finance-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "finance@company.com";
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        user.setFullName("Finance Manager");
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
        when(roleService.hasRoleOrGreater(any(User.class), eq("FINANCE"))).thenReturn(true);
    }

    @Test
    public void testGetDashboard() throws Exception {
        ExpenseDashboardResponse data = new ExpenseDashboardResponse(36L, BigDecimal.valueOf(154000), 84L, BigDecimal.valueOf(128400), 12L, BigDecimal.valueOf(38000), 1.8);
        when(financeExpenseService.getDashboard()).thenReturn(data);

        mockMvc.perform(get("/api/v1/finance/expenses/dashboard")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalPending").value(36))
                .andExpect(jsonPath("$.data.approvedThisMonth").value(84))
                .andExpect(jsonPath("$.data.approvedAmountThisMonth").value(128400))
                .andExpect(jsonPath("$.data.rejected").value(12))
                .andExpect(jsonPath("$.data.averageApprovalDays").value(1.8));
    }

    @Test
    public void testGetExpensesList() throws Exception {
        FinanceExpenseListItem item = new FinanceExpenseListItem(
                101L, 15L, "Robert Chen", "Engineering", "TRAVEL",
                "Flight to Delhi - Client Meeting", BigDecimal.valueOf(4200), true,
                LocalDate.of(2026, 4, 3), "PENDING"
        );
        FinanceExpenseListResponse data = new FinanceExpenseListResponse(List.of(item), 36L, 2);
        when(financeExpenseService.getExpenses(any(), any(), any(), any(), any(), any(), any())).thenReturn(data);

        mockMvc.perform(get("/api/v1/finance/expenses")
                        .header("Authorization", AUTH_HEADER)
                        .param("status", "PENDING")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(36))
                .andExpect(jsonPath("$.data.content[0].employeeName").value("Robert Chen"));
    }

    @Test
    public void testGetExpenseDetails() throws Exception {
        FinanceExpenseDetailsResponse.EmployeeInfo emp = new FinanceExpenseDetailsResponse.EmployeeInfo(15L, "Robert Chen", "Engineering");
        FinanceExpenseDetailsResponse details = new FinanceExpenseDetailsResponse(
                101L, emp, "TRAVEL", "Flight to Delhi - Client Meeting", BigDecimal.valueOf(4200),
                "Client Meeting", LocalDate.of(2026, 4, 3), "PENDING", true, "/api/v1/files/receipts/receipt-101.pdf"
        );
        when(financeExpenseService.getExpenseDetails(101L)).thenReturn(details);

        mockMvc.perform(get("/api/v1/finance/expenses/101")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.businessPurpose").value("Client Meeting"))
                .andExpect(jsonPath("$.data.employee.name").value("Robert Chen"));
    }

    @Test
    public void testGetReceiptMetadata() throws Exception {
        FinanceExpenseReceiptResponse data = new FinanceExpenseReceiptResponse("receipt-101.pdf", "application/pdf", 254321L, "/api/v1/files/receipt-101.pdf");
        when(financeExpenseService.getReceiptMetadata(101L)).thenReturn(data);

        mockMvc.perform(get("/api/v1/finance/expenses/101/receipt")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("receipt-101.pdf"))
                .andExpect(jsonPath("$.data.downloadUrl").value("/api/v1/files/receipt-101.pdf"));
    }

    @Test
    public void testApproveExpense() throws Exception {
        doNothing().when(financeExpenseService).approveExpense(eq(101L), eq("Expense verified and approved"), any());

        ApproveRequest req = new ApproveRequest("Expense verified and approved");
        mockMvc.perform(patch("/api/v1/finance/expenses/101/approve")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    public void testRejectExpense() throws Exception {
        doNothing().when(financeExpenseService).rejectExpense(eq(101L), eq("Receipt missing"), any());

        RejectRequest req = new RejectRequest("Receipt missing");
        mockMvc.perform(patch("/api/v1/finance/expenses/101/reject")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    public void testSendBackExpense() throws Exception {
        doNothing().when(financeExpenseService).sendBackExpense(eq(101L), eq("Upload original invoice copy"), any());

        SendBackRequest req = new SendBackRequest("Upload original invoice copy");
        mockMvc.perform(patch("/api/v1/finance/expenses/101/send-back")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT_BACK"));
    }

    @Test
    public void testGetTimeline() throws Exception {
        FinanceExpenseTimelineItem item = new FinanceExpenseTimelineItem("SUBMITTED", "Robert Chen", LocalDateTime.now());
        when(financeExpenseService.getTimeline(101L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/finance/expenses/101/timeline")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].status").value("SUBMITTED"));
    }

    @Test
    public void testGetReportsSummary() throws Exception {
        ExpenseReportsSummaryResponse summary = new ExpenseReportsSummaryResponse(245, 184, 36, 25, BigDecimal.valueOf(1250000), BigDecimal.valueOf(950000), 1.8);
        when(financeExpenseService.getReportsSummary()).thenReturn(summary);

        mockMvc.perform(get("/api/v1/finance/expenses/reports/summary")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.approvedExpenses").value(184));
    }

    @Test
    public void testExportCsv() throws Exception {
        byte[] csvData = "Simulated CSV".getBytes();
        when(financeExpenseService.exportCsv(any(), any(), any(), any())).thenReturn(csvData);

        mockMvc.perform(get("/api/v1/finance/expenses/export/csv")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String type = result.getResponse().getContentType();
                    assert type != null && type.contains("text/csv");
                });
    }

    @Test
    public void testExportXlsx() throws Exception {
        byte[] xlsxData = "Simulated XLSX".getBytes();
        when(financeExpenseService.exportXlsx(any(), any(), any(), any())).thenReturn(xlsxData);

        mockMvc.perform(get("/api/v1/finance/expenses/export/xlsx")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String type = result.getResponse().getContentType();
                    assert type != null && type.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                });
    }

    @Test
    public void testExportPdf() throws Exception {
        byte[] pdfData = "Simulated PDF".getBytes();
        when(financeExpenseService.exportPdf(any(), any(), any(), any())).thenReturn(pdfData);

        mockMvc.perform(get("/api/v1/finance/expenses/export/pdf")
                        .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String type = result.getResponse().getContentType();
                    assert type != null && type.contains("application/pdf");
                });
    }

    @Test
    public void testReimburseExpense() throws Exception {
        ReimburseExpenseResponse resp = new ReimburseExpenseResponse(101L, "REIMBURSED", "BANK_TRANSFER", "TXN202600123");
        when(financeExpenseService.reimburseExpense(eq(101L), eq("BANK_TRANSFER"), eq("TXN202600123"), any())).thenReturn(resp);

        ReimburseExpenseRequest req = new ReimburseExpenseRequest("BANK_TRANSFER", "TXN202600123");
        mockMvc.perform(patch("/api/v1/finance/expenses/101/reimburse")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REIMBURSED"))
                .andExpect(jsonPath("$.transactionReference").value("TXN202600123"));
    }

    @Test
    public void testBulkApprove() throws Exception {
        doNothing().when(financeExpenseService).bulkApprove(any(), any(), any());

        BulkExpenseRequest req = new BulkExpenseRequest(List.of(101L, 102L), "Bulk approved remarks");
        mockMvc.perform(patch("/api/v1/finance/expenses/bulk-approve")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testBulkReject() throws Exception {
        doNothing().when(financeExpenseService).bulkReject(any(), any(), any());

        BulkExpenseRequest req = new BulkExpenseRequest(List.of(101L, 102L), "Bulk rejected reason");
        mockMvc.perform(patch("/api/v1/finance/expenses/bulk-reject")
                        .header("Authorization", AUTH_HEADER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
