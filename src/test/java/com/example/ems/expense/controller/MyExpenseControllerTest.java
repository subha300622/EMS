package com.example.ems.expense.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.dto.*;
import com.example.ems.expense.entity.*;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.expense.repository.MyExpenseReceiptRepository;
import com.example.ems.expense.service.MyExpenseService;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MyExpenseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MyExpenseService expenseService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private MyExpenseReceiptRepository receiptRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private MyExpenseController myExpenseController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "employee@example.com";
    private Employee mockEmployee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(myExpenseController).build();

        // Standard auth mock setup
        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);

        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));

        mockEmployee = new Employee();
        mockEmployee.setId(1L);
        mockEmployee.setEmployeeId("EMP001");
        mockEmployee.setFullName("John Doe");
        mockEmployee.setEmail(EMAIL);
        mockEmployee.setDepartment("Engineering");
        when(employeeRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockEmployee));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }



    @Test
    public void testGetMyExpensesSuccess() throws Exception {
        mockPermission("expense.self.read", true);

        MyExpenseItem item = new MyExpenseItem(1001L, "EXP-2026-0001", "TRAVEL", "Client Meeting", LocalDate.now(), BigDecimal.valueOf(3500), "INR", "PENDING_MANAGER_APPROVAL", LocalDateTime.now(), "NOT_PAID", new MyExpenseItem.ActionInfo(false, true, true));
        MyExpenseListResponse response = new MyExpenseListResponse(List.of(item), new MyExpenseListResponse.PaginationInfo(0, 10, 1, 1, false, false));

        when(expenseService.getMyExpenses(any(), any(), any(), any(), any(), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-expenses")
                .header("Authorization", AUTH_HEADER)
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Client Meeting"));
    }

    @Test
    public void testCreateExpenseSuccess() throws Exception {
        mockPermission("expense.self.create", true);

        CreateExpenseRequest request = new CreateExpenseRequest("TRAVEL", "Client Visit", "Notes", LocalDate.now(), BigDecimal.valueOf(4500.00), "INR", "PRJ-101", List.of(501L));
        CreateExpenseResponse response = new CreateExpenseResponse(1001L, "EXP-2026-0001", "PENDING_MANAGER_APPROVAL", LocalDateTime.now(), "Expense claim submitted successfully");

        when(expenseService.createExpense(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/my-expenses")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.expenseNumber").value("EXP-2026-0001"));
    }

    @Test
    public void testGetExpenseDetailsSuccess() throws Exception {
        mockPermission("expense.self.read", true);

        Expense exp = new Expense();
        exp.setId(1001L);
        exp.setEmployee(mockEmployee);
        when(expenseRepository.findById(1001L)).thenReturn(Optional.of(exp));

        ExpenseDetailsResponse response = new ExpenseDetailsResponse(
                1001L, "EXP-2026-0001", new ExpenseDetailsResponse.EmployeeInfo(1L, "John Doe"), "TRAVEL",
                "Client Meeting", "Taxi and food", LocalDate.now(), BigDecimal.valueOf(3500.0), "INR",
                List.of(new ExpenseDetailsResponse.ReceiptInfo(501L, "bill.pdf", LocalDateTime.now())),
                List.of(new ExpenseDetailsResponse.ApprovalStepInfo(1, "MANAGER", "APPROVED", LocalDateTime.now(), "Approved")),
                new ExpenseDetailsResponse.PaymentInfo("PENDING", "May 2026")
        );

        when(expenseService.getExpenseDetails(eq(1001L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-expenses/1001")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Client Meeting"));
    }

    @Test
    public void testUpdateExpenseSuccess() throws Exception {
        mockPermission("expense.self.update", true);

        Expense exp = new Expense();
        exp.setId(1001L);
        exp.setEmployee(mockEmployee);
        exp.setStatus("DRAFT");
        when(expenseRepository.findById(1001L)).thenReturn(Optional.of(exp));

        UpdateExpenseRequest request = new UpdateExpenseRequest("Updated client visit", "Notes", BigDecimal.valueOf(4000.0), List.of(503L));
        UpdateExpenseResponse response = new UpdateExpenseResponse(1001L, "UPDATED", LocalDateTime.now(), "Expense updated successfully");

        when(expenseService.updateExpense(eq(1001L), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/my-expenses/1001")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UPDATED"));
    }

    @Test
    public void testWithdrawExpenseSuccess() throws Exception {
        mockPermission("expense.self.withdraw", true);

        Expense exp = new Expense();
        exp.setId(1001L);
        exp.setEmployee(mockEmployee);
        exp.setStatus("PENDING_MANAGER_APPROVAL");
        when(expenseRepository.findById(1001L)).thenReturn(Optional.of(exp));

        WithdrawExpenseRequest request = new WithdrawExpenseRequest("Incorrect amount");
        WithdrawExpenseResponse response = new WithdrawExpenseResponse(1001L, "WITHDRAWN", LocalDateTime.now(), "Expense claim withdrawn successfully");

        when(expenseService.withdrawExpense(eq(1001L), any(), any())).thenReturn(response);

        mockMvc.perform(patch("/api/v1/my-expenses/1001/withdraw")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("WITHDRAWN"));
    }

    @Test
    public void testUploadReceiptSuccess() throws Exception {
        mockPermission("expense.self.receipt.upload", true);

        MockMultipartFile file = new MockMultipartFile("file", "bill.pdf", MediaType.APPLICATION_PDF_VALUE, "dummy bytes".getBytes());
        UploadReceiptResponse response = new UploadReceiptResponse(501L, "bill.pdf", MediaType.APPLICATION_PDF_VALUE, 256000L, LocalDateTime.now());

        when(expenseService.uploadReceipt(any(), any(), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/my-expenses/receipts")
                .file(file)
                .param("receiptType", "INVOICE")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.receiptId").value(501L));
    }

    @Test
    public void testDownloadReceiptSuccess() throws Exception {
        mockPermission("expense.self.read", true);

        MyExpenseReceipt receipt = new MyExpenseReceipt();
        receipt.setId(501L);
        receipt.setEmployee(mockEmployee);
        receipt.setFileName("bill.pdf");
        receipt.setFileType("application/pdf");
        receipt.setFileData("dummy".getBytes());
        when(receiptRepository.findById(501L)).thenReturn(Optional.of(receipt));
        when(expenseService.downloadReceipt(501L)).thenReturn(receipt);

        mockMvc.perform(get("/api/v1/my-expenses/receipts/501/download")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(status().is(200));
    }

    @Test
    public void testGetExpenseTimelineSuccess() throws Exception {
        mockPermission("expense.self.timeline.read", true);

        Expense exp = new Expense();
        exp.setId(1001L);
        exp.setEmployee(mockEmployee);
        when(expenseRepository.findById(1001L)).thenReturn(Optional.of(exp));

        ExpenseTimelineResponse.TimelineEventItem event = new ExpenseTimelineResponse.TimelineEventItem("CREATED", "John Doe", LocalDateTime.now());
        ExpenseTimelineResponse response = new ExpenseTimelineResponse(1001L, List.of(event));

        when(expenseService.getExpenseTimeline(eq(1001L), any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-expenses/1001/timeline")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timeline[0].event").value("CREATED"));
    }

    @Test
    public void testGetCategoriesSuccess() throws Exception {
        mockPermission("expense.self.read", true);

        ExpenseCategoriesResponse.CategoryItem cat = new ExpenseCategoriesResponse.CategoryItem("TRAVEL", "Travel", BigDecimal.valueOf(10000), true);
        ExpenseCategoriesResponse response = new ExpenseCategoriesResponse(List.of(cat));

        when(expenseService.getCategories()).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-expenses/categories")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.categories[0].code").value("TRAVEL"));
    }

    @Test
    public void testGetPoliciesSuccess() throws Exception {
        mockPermission("expense.self.read", true);

        ExpensePoliciesResponse.PolicyItem policy = new ExpensePoliciesResponse.PolicyItem(1L, "Travel Reimbursement Policy", "Details", LocalDate.now(), "1.0");
        ExpensePoliciesResponse response = new ExpensePoliciesResponse(List.of(policy));

        when(expenseService.getPolicies()).thenReturn(response);

        mockMvc.perform(get("/api/v1/my-expenses/policies")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.policies[0].title").value("Travel Reimbursement Policy"));
    }
}
