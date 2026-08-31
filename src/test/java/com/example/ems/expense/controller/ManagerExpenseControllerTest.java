package com.example.ems.expense.controller;

import com.example.ems.approval.dto.ApprovalTaskDto;
import com.example.ems.approval.entity.ApprovalStatus;
import com.example.ems.approval.entity.ApprovalTask;
import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.repository.ApprovalTaskRepository;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.expense.dto.ApproveExpenseRequest;
import com.example.ems.expense.dto.ExpenseRejectRequest;
import com.example.ems.expense.entity.Expense;
import com.example.ems.expense.repository.ExpenseRepository;
import com.example.ems.expense.service.MyExpenseService;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ManagerExpenseControllerTest {

        private MockMvc mockMvc;
        private ObjectMapper objectMapper = new ObjectMapper();

        @Mock
        private ApprovalWorkflowEngineService approvalWorkflowEngineService;

        @Mock
        private ApprovalTaskRepository taskRepository;

        @Mock
        private ExpenseRepository expenseRepository;

        @Mock
        private MyExpenseService myExpenseService;

        @Mock
        private UserRepository userRepository;

        @Mock
        private EmployeeRepository employeeRepository;

        @Mock
        private JwtService jwtService;

        @Mock
        private RoleService roleService;

        @InjectMocks
        private ManagerExpenseController managerExpenseController;

        private static final String TOKEN_MANAGER_A = "mock-token-manager-a";
        private static final String AUTH_HEADER_A = "Bearer " + TOKEN_MANAGER_A;
        private static final String EMAIL_MANAGER_A = "manager.a@example.com";

        private static final String TOKEN_MANAGER_B = "mock-token-manager-b";
        private static final String AUTH_HEADER_B = "Bearer " + TOKEN_MANAGER_B;
        private static final String EMAIL_MANAGER_B = "manager.b@example.com";

        private Employee managerA;
        private Employee managerB;
        private User userManagerA;
        private User userManagerB;

        @BeforeEach
        public void setUp() {
                MockitoAnnotations.openMocks(this);
                objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                mockMvc = MockMvcBuilders.standaloneSetup(managerExpenseController).build();

                // Setup Manager A
                when(jwtService.validateAccessToken(TOKEN_MANAGER_A)).thenReturn(true);
                when(jwtService.getEmailFromToken(TOKEN_MANAGER_A)).thenReturn(EMAIL_MANAGER_A);
                userManagerA = new User();
                userManagerA.setWorkEmail(EMAIL_MANAGER_A);
                when(userRepository.findByWorkEmail(EMAIL_MANAGER_A)).thenReturn(Optional.of(userManagerA));
                managerA = new Employee();
                managerA.setId(10L);
                managerA.setEmployeeId("MGR001");
                managerA.setFullName("Manager A");
                managerA.setEmail(EMAIL_MANAGER_A);
                when(employeeRepository.findByEmail(EMAIL_MANAGER_A)).thenReturn(Optional.of(managerA));

                // Setup Manager B
                when(jwtService.validateAccessToken(TOKEN_MANAGER_B)).thenReturn(true);
                when(jwtService.getEmailFromToken(TOKEN_MANAGER_B)).thenReturn(EMAIL_MANAGER_B);
                userManagerB = new User();
                userManagerB.setWorkEmail(EMAIL_MANAGER_B);
                when(userRepository.findByWorkEmail(EMAIL_MANAGER_B)).thenReturn(Optional.of(userManagerB));
                managerB = new Employee();
                managerB.setId(20L);
                managerB.setEmployeeId("MGR002");
                managerB.setFullName("Manager B");
                managerB.setEmail(EMAIL_MANAGER_B);
                when(employeeRepository.findByEmail(EMAIL_MANAGER_B)).thenReturn(Optional.of(managerB));
        }

        private void mockPermission(String email, String permission, boolean allowed) {
                when(roleService.hasPermission(email, permission)).thenReturn(allowed);
        }

        @Test
        public void testGetManagerExpensesSuccess() throws Exception {
                mockPermission(EMAIL_MANAGER_A, "expense.approval.read", true);

                Organization org = new Organization();
                org.setId(1L);

                ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
                instance.setOrganization(org);
                instance.setWorkflowInstanceId("WFI-001");

                ApprovalTask task = new ApprovalTask();
                task.setApprovalTaskId("AT-001");
                task.setWorkflowInstance(instance);
                task.setWorkflowType(WorkflowType.EXPENSE_APPROVAL);
                task.setBusinessReferenceType("EXPENSE");
                task.setBusinessReferenceId("1001");
                task.setApprover(managerA);
                task.setStatus(ApprovalStatus.PENDING);

                when(taskRepository.findInboxTasks(eq(10L), any(), eq(WorkflowType.EXPENSE_APPROVAL),
                                eq(ApprovalStatus.PENDING), any()))
                                .thenReturn(new PageImpl<>(List.of(task)));

                Expense exp = new Expense();
                exp.setId(1001L);
                exp.setExpenseNumber("EXP-2026-0001");
                exp.setTitle("Travel Claim");
                exp.setAmount(BigDecimal.valueOf(4500));
                exp.setStatus("PENDING_MANAGER_APPROVAL");
                when(expenseRepository.findById(1001L)).thenReturn(Optional.of(exp));

                mockMvc.perform(get("/api/v1/manager/expenses")
                                .header("Authorization", AUTH_HEADER_A))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content[0].expenseNumber").value("EXP-2026-0001"));
        }

        @Test
        public void testGetManagerExpensesSuccessForManagerB() throws Exception {
                mockPermission(EMAIL_MANAGER_B, "expense.approval.read", true);

                Organization org = new Organization();
                org.setId(1L);

                ApprovalWorkflowInstance instance = new ApprovalWorkflowInstance();
                instance.setOrganization(org);
                instance.setWorkflowInstanceId("WFI-002");

                ApprovalTask task = new ApprovalTask();
                task.setApprovalTaskId("AT-002");
                task.setWorkflowInstance(instance);
                task.setWorkflowType(WorkflowType.EXPENSE_APPROVAL);
                task.setBusinessReferenceType("EXPENSE");
                task.setBusinessReferenceId("2002");
                task.setApprover(managerB);
                task.setStatus(ApprovalStatus.PENDING);

                when(taskRepository.findInboxTasks(eq(20L), any(), eq(WorkflowType.EXPENSE_APPROVAL),
                                eq(ApprovalStatus.PENDING), any()))
                                .thenReturn(new PageImpl<>(List.of(task)));

                Expense exp = new Expense();
                exp.setId(2002L);
                exp.setExpenseNumber("EXP-2026-0002");
                exp.setTitle("Equipment Claim");
                exp.setAmount(BigDecimal.valueOf(8500));
                exp.setStatus("PENDING_MANAGER_APPROVAL");
                when(expenseRepository.findById(2002L)).thenReturn(Optional.of(exp));

                mockMvc.perform(get("/api/v1/manager/expenses")
                                .header("Authorization", AUTH_HEADER_B))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.content[0].expenseNumber").value("EXP-2026-0002"));
        }

        @Test
        public void testApproveExpenseSuccess() throws Exception {
                mockPermission(EMAIL_MANAGER_A, "expense.approval.approve", true);

                ApprovalTask task = new ApprovalTask();
                task.setApprovalTaskId("AT-001");
                task.setApprover(managerA);
                task.setStatus(ApprovalStatus.PENDING);

                when(taskRepository.findActiveTasksForBusinessRef(WorkflowType.EXPENSE_APPROVAL, "EXPENSE", "1001"))
                                .thenReturn(List.of(task));

                ApprovalTaskDto dto = new ApprovalTaskDto("AT-001", "WFI-001", WorkflowType.EXPENSE_APPROVAL, "EXPENSE",
                                "1001", 1, "Manager Approval", "MGR001", "Manager A", ApprovalStatus.APPROVED, null,
                                null);
                when(approvalWorkflowEngineService.approveTask(any(), eq("AT-001"), any())).thenReturn(dto);

                ApproveExpenseRequest req = new ApproveExpenseRequest("Approved by manager");

                mockMvc.perform(patch("/api/v1/manager/expenses/1001/approve")
                                .header("Authorization", AUTH_HEADER_A)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        public void testManagerATriesToApproveManagerBExpense_ReturnsForbidden() throws Exception {
                // Task belongs to Manager B
                ApprovalTask taskForManagerB = new ApprovalTask();
                taskForManagerB.setApprovalTaskId("AT-002");
                taskForManagerB.setApprover(managerB);
                taskForManagerB.setStatus(ApprovalStatus.PENDING);

                when(taskRepository.findActiveTasksForBusinessRef(WorkflowType.EXPENSE_APPROVAL, "EXPENSE", "2002"))
                                .thenReturn(List.of(taskForManagerB));

                mockPermission(EMAIL_MANAGER_A, "expense.approval.approve", true);

                // Manager A attempts to approve expense 2002 assigned to Manager B
                ApproveExpenseRequest req = new ApproveExpenseRequest("Unauthorized approval attempt");

                mockMvc.perform(patch("/api/v1/manager/expenses/2002/approve")
                                .header("Authorization", AUTH_HEADER_A)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.success").value(false))
                                .andExpect(jsonPath("$.error.code").value("EXP_403"));
        }

        @Test
        public void testRejectExpenseSuccess() throws Exception {
                mockPermission(EMAIL_MANAGER_A, "expense.approval.approve", true);

                ApprovalTask task = new ApprovalTask();
                task.setApprovalTaskId("AT-001");
                task.setApprover(managerA);
                task.setStatus(ApprovalStatus.PENDING);

                when(taskRepository.findActiveTasksForBusinessRef(WorkflowType.EXPENSE_APPROVAL, "EXPENSE", "1001"))
                                .thenReturn(List.of(task));

                ApprovalTaskDto dto = new ApprovalTaskDto("AT-001", "WFI-001", WorkflowType.EXPENSE_APPROVAL, "EXPENSE",
                                "1001", 1, "Manager Approval", "MGR001", "Manager A", ApprovalStatus.REJECTED, null,
                                null);
                when(approvalWorkflowEngineService.rejectTask(any(), eq("AT-001"), any())).thenReturn(dto);

                ExpenseRejectRequest req = new ExpenseRejectRequest("Policy violation");

                mockMvc.perform(patch("/api/v1/manager/expenses/1001/reject")
                                .header("Authorization", AUTH_HEADER_A)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        public void testSendBackExpenseSuccess() throws Exception {
                mockPermission(EMAIL_MANAGER_A, "expense.approval.approve", true);

                ApprovalTask task = new ApprovalTask();
                task.setApprovalTaskId("AT-001");
                task.setApprover(managerA);
                task.setStatus(ApprovalStatus.PENDING);

                when(taskRepository.findActiveTasksForBusinessRef(WorkflowType.EXPENSE_APPROVAL, "EXPENSE", "1001"))
                                .thenReturn(List.of(task));

                ApprovalTaskDto dto = new ApprovalTaskDto("AT-001", "WFI-001", WorkflowType.EXPENSE_APPROVAL, "EXPENSE",
                                "1001", 1, "Manager Approval", "MGR001", "Manager A", ApprovalStatus.REQUEST_CHANGES,
                                null, null);
                when(approvalWorkflowEngineService.requestChanges(any(), eq("AT-001"), any())).thenReturn(dto);

                ExpenseRejectRequest req = new ExpenseRejectRequest("Attach missing bill");

                mockMvc.perform(patch("/api/v1/manager/expenses/1001/send-back")
                                .header("Authorization", AUTH_HEADER_A)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }
}
