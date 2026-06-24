package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.Increment;
import com.example.ems.appraisal.entity.SalaryRevision;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SalaryRevisionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AppraisalService appraisalService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private SalaryRevisionController salaryRevisionController;

    private User adminUser;
    private final String token = "Bearer mock-token";
    private final String email = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(salaryRevisionController).build();

        adminUser = new User();
        adminUser.setWorkEmail(email);
        adminUser.setEmployeeId("1");
        Role role = new Role();
        role.setName("ADMIN");
        adminUser.setRole(role);
    }

    private void mockAuthSuccess() {
        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn(email);
        when(userRepository.findByWorkEmail(email)).thenReturn(Optional.of(adminUser));
        when(roleService.hasRole(any(User.class), any(String.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            String r = invocation.getArgument(1);
            return u != null && u.getRole() != null && r.equals(u.getRole().getName());
        });
        when(roleService.hasRoleOrGreater(any(User.class), any(String.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            String r = invocation.getArgument(1);
            if (u == null || u.getRole() == null || r == null) return false;
            java.util.Map<String, Integer> hierarchy = java.util.Map.of(
                "SUPER_ADMIN", 1,
                "ADMIN", 2,
                "HR", 3,
                "MANAGER", 4,
                "FINANCE", 5,
                "EMPLOYEE", 6
            );
            Integer userLevel = hierarchy.get(u.getRole().getName());
            Integer targetLevel = hierarchy.get(r);
            if (userLevel == null || targetLevel == null) return false;
            return userLevel <= targetLevel;
        });
    }

    @Test
    public void testCreateIncrementSuccess() throws Exception {
        mockAuthSuccess();
        NewIncrementRequest request = new NewIncrementRequest();
        request.setEmployeeId("EMP002");
        request.setIncrementPercentage(BigDecimal.valueOf(10));
        request.setEffectiveDate(LocalDate.now());

        Employee emp = new Employee();
        emp.setId(2L);
        emp.setEmployeeId("EMP002");
        emp.setFullName("Jane Doe");

        Increment inc = new Increment();
        inc.setId(1L);
        inc.setEmployee(emp);
        inc.setIncrementPercentage(BigDecimal.valueOf(10));
        inc.setCurrentSalary(BigDecimal.valueOf(1000));
        inc.setNewSalary(BigDecimal.valueOf(1100));
        inc.setStatus("PENDING");
        inc.setCreatedAt(LocalDateTime.now());

        when(appraisalService.createIncrement(any(NewIncrementRequest.class))).thenReturn(inc);

        mockMvc.perform(post("/api/v1/salary-revisions")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revisionId").value("REV001"));
    }

    @Test
    public void testGetSalaryRevisionsSuccess() throws Exception {
        mockAuthSuccess();
        IncrementResponse incResp = new IncrementResponse();
        incResp.setId(1L);

        Employee emp = new Employee();
        emp.setId(2L);
        emp.setEmployeeId("EMP002");
        emp.setFullName("Jane Doe");

        Increment inc = new Increment();
        inc.setId(1L);
        inc.setEmployee(emp);
        inc.setStatus("PENDING");

        Page<IncrementResponse> page = new PageImpl<>(List.of(incResp));
        when(appraisalService.getSalaryRevisions(eq("PENDING"), any(Pageable.class))).thenReturn(page);
        when(appraisalService.getIncrementEntityById(1L)).thenReturn(Optional.of(inc));

        mockMvc.perform(get("/api/v1/salary-revisions")
                .header("Authorization", token)
                .param("page", "0")
                .param("size", "10")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].revisionId").value("REV001"));
    }

    @Test
    public void testGetSalaryRevisionDetailsSuccess() throws Exception {
        mockAuthSuccess();
        
        Employee emp = new Employee();
        emp.setId(1L); // Self access since adminUser employeeId is "1"
        emp.setEmployeeId("1");

        Increment inc = new Increment();
        inc.setId(1L);
        inc.setEmployee(emp);
        inc.setCurrentSalary(BigDecimal.valueOf(1000));
        inc.setIncrementPercentage(BigDecimal.valueOf(10));
        inc.setNewSalary(BigDecimal.valueOf(1100));
        inc.setStatus("PENDING");
        inc.setCreatedAt(LocalDateTime.now());

        when(appraisalService.getIncrementEntityById(1L)).thenReturn(Optional.of(inc));

        mockMvc.perform(get("/api/v1/salary-revisions/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.revisionId").value("REV001"));
    }

    @Test
    public void testUpdateIncrementSuccess() throws Exception {
        mockAuthSuccess();
        NewIncrementRequest request = new NewIncrementRequest();
        request.setEmployeeId("EMP002");
        request.setIncrementPercentage(BigDecimal.valueOf(12));
        request.setEffectiveDate(LocalDate.now());

        Increment inc = new Increment();
        inc.setId(1L);
        inc.setIncrementPercentage(BigDecimal.valueOf(12));
        inc.setNewSalary(BigDecimal.valueOf(1120));
        inc.setStatus("PENDING");

        when(appraisalService.updateIncrement(eq(1L), any(BigDecimal.class), any(LocalDate.class), any()))
            .thenReturn(Optional.of(inc));

        mockMvc.perform(put("/api/v1/salary-revisions/1")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.incrementPercentage").value(12));
    }

    @Test
    public void testApproveIncrementSuccess() throws Exception {
        mockAuthSuccess();
        Increment inc = new Increment();
        inc.setId(1L);
        inc.setStatus("APPROVED");
        inc.setApprovedAt(LocalDateTime.now());

        when(appraisalService.approveIncrementEntity(1L, email)).thenReturn(Optional.of(inc));

        mockMvc.perform(patch("/api/v1/salary-revisions/1/approve")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testRejectIncrementSuccess() throws Exception {
        mockAuthSuccess();
        SalaryRevisionRejectRequest request = new SalaryRevisionRejectRequest();
        request.setReason("Performance criteria not met");

        Increment inc = new Increment();
        inc.setId(1L);
        inc.setStatus("REJECTED");
        inc.setApprovedAt(LocalDateTime.now());
        inc.setReason("Performance criteria not met");

        when(appraisalService.rejectIncrementEntity(1L, email, "Performance criteria not met")).thenReturn(Optional.of(inc));

        mockMvc.perform(patch("/api/v1/salary-revisions/1/reject")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"))
                .andExpect(jsonPath("$.data.reason").value("Performance criteria not met"));
    }

    @Test
    public void testApplyIncrementSuccess() throws Exception {
        mockAuthSuccess();
        Increment inc = new Increment();
        inc.setId(1L);
        inc.setStatus("APPLIED");
        inc.setAppliedAt(LocalDateTime.now());

        when(appraisalService.applyIncrementEntity(1L)).thenReturn(Optional.of(inc));

        mockMvc.perform(post("/api/v1/salary-revisions/1/apply")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPLIED"));
    }

    @Test
    public void testGetSalaryHistorySuccess() throws Exception {
        mockAuthSuccess();
        
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmployeeId("1");

        SalaryRevision revision = new SalaryRevision();
        revision.setId(1L);
        revision.setEmployee(emp);
        revision.setPreviousSalary(BigDecimal.valueOf(4000));
        revision.setNewSalary(BigDecimal.valueOf(5000));
        revision.setChangePercentage(BigDecimal.valueOf(25));
        revision.setEffectiveDate(LocalDate.now());

        when(appraisalService.getSalaryRevisionEntities(1L)).thenReturn(List.of(revision));

        mockMvc.perform(get("/api/v1/employees/1/salary-history")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].revisionId").value("REV001"));
    }

    @Test
    public void testGetIncrementLetterSuccess() throws Exception {
        mockAuthSuccess();
        IncrementLetterResponse letter = new IncrementLetterResponse();
        letter.setEmployeeId("EMP020");
        letter.setEmployeeName("Jane Doe");
        letter.setLetterBody("SUBJECT: SALARY REVISION CONFIRMATION\n\nDear Jane Doe...");

        when(appraisalService.getIncrementLetter(20L)).thenReturn(letter);

        mockMvc.perform(get("/api/v1/salary-revisions/20/letter")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeName").value("Jane Doe"));
    }

    @Test
    public void testGetIncrementLetterSelfSuccess() throws Exception {
        Role role = new Role();
        role.setName("EMPLOYEE");
        adminUser.setRole(role);
        mockAuthSuccess();

        IncrementLetterResponse letter = new IncrementLetterResponse();
        letter.setEmployeeId("EMP020");
        letter.setEmployeeName("Jane Doe");
        letter.setLetterBody("Dear Jane Doe...");

        Employee emp = new Employee();
        emp.setFullName("Jane Doe");
        emp.setEmail(email);

        when(appraisalService.getIncrementLetter(20L)).thenReturn(letter);
        when(employeeRepository.findByEmail(email)).thenReturn(Optional.of(emp));

        mockMvc.perform(get("/api/v1/salary-revisions/20/letter")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeName").value("Jane Doe"));

        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminUser.setRole(adminRole);
    }

    @Test
    public void testExecutePayrollSuccess() throws Exception {
        mockAuthSuccess();
        SalaryRevision rev = new SalaryRevision();
        rev.setId(10L);
        rev.setPreviousSalary(BigDecimal.valueOf(1000));
        rev.setNewSalary(BigDecimal.valueOf(1100));

        when(appraisalService.executePayrollDecoupled(eq(5L), any())).thenReturn(rev);

        mockMvc.perform(post("/api/v1/payroll-revisions/appraisals/5/execute")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }

    @Test
    public void testRetryPayrollSuccess() throws Exception {
        mockAuthSuccess();
        SalaryRevision rev = new SalaryRevision();
        rev.setId(10L);
        rev.setPreviousSalary(BigDecimal.valueOf(1000));
        rev.setNewSalary(BigDecimal.valueOf(1100));

        when(appraisalService.retryPayroll(eq(5L), any())).thenReturn(rev);

        mockMvc.perform(post("/api/v1/payroll-revisions/appraisals/5/retry")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }
}

