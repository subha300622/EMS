package com.example.ems.appraisal.controller;

import com.example.ems.appraisal.dto.IncrementLetterResponse;
import com.example.ems.appraisal.dto.IncrementRequest;
import com.example.ems.appraisal.dto.IncrementResponse;
import com.example.ems.appraisal.dto.SalaryRevisionResponse;
import com.example.ems.appraisal.service.AppraisalService;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

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
        IncrementRequest request = new IncrementRequest();
        request.setEmployeeId(2L);
        request.setIncrementPercentage(BigDecimal.valueOf(10));
        request.setEffectiveDate(LocalDate.now());

        IncrementResponse response = new IncrementResponse();
        response.setId(1L);
        response.setEmployeeId(2L);
        response.setIncrementPercentage(BigDecimal.valueOf(10));

        when(appraisalService.createIncrement(any(IncrementRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/salary-revisions")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testGetSalaryRevisionsSuccess() throws Exception {
        mockAuthSuccess();
        IncrementResponse inc = new IncrementResponse();
        inc.setId(1L);
        inc.setStatus("PENDING");

        Page<IncrementResponse> page = new PageImpl<>(List.of(inc));
        when(appraisalService.getSalaryRevisions(eq("PENDING"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/salary-revisions")
                .header("Authorization", token)
                .param("page", "0")
                .param("size", "10")
                .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));
    }

    @Test
    public void testGetSalaryRevisionDetailsSuccess() throws Exception {
        mockAuthSuccess();
        IncrementResponse inc = new IncrementResponse();
        inc.setId(1L);
        inc.setEmployeeId(1L); // Self access

        when(appraisalService.getIncrementById(1L)).thenReturn(Optional.of(inc));

        mockMvc.perform(get("/api/v1/salary-revisions/1")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    public void testUpdateIncrementSuccess() throws Exception {
        mockAuthSuccess();
        IncrementRequest request = new IncrementRequest();
        request.setEmployeeId(2L);
        request.setIncrementPercentage(BigDecimal.valueOf(12));
        request.setEffectiveDate(LocalDate.now());

        IncrementResponse response = new IncrementResponse();
        response.setId(1L);
        response.setIncrementPercentage(BigDecimal.valueOf(12));

        when(appraisalService.updateIncrement(eq(1L), any(IncrementRequest.class))).thenReturn(Optional.of(response));

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
        IncrementResponse response = new IncrementResponse();
        response.setId(1L);
        response.setStatus("APPROVED");

        when(appraisalService.approveIncrement(1L, email)).thenReturn(Optional.of(response));

        mockMvc.perform(patch("/api/v1/salary-revisions/1/approve")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    public void testRejectIncrementSuccess() throws Exception {
        mockAuthSuccess();
        IncrementResponse response = new IncrementResponse();
        response.setId(1L);
        response.setStatus("REJECTED");

        when(appraisalService.rejectIncrement(1L, email)).thenReturn(Optional.of(response));

        mockMvc.perform(patch("/api/v1/salary-revisions/1/reject")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));
    }

    @Test
    public void testApplyIncrementSuccess() throws Exception {
        mockAuthSuccess();
        IncrementResponse response = new IncrementResponse();
        response.setId(1L);
        response.setStatus("APPLIED");

        when(appraisalService.applyIncrement(1L)).thenReturn(Optional.of(response));

        mockMvc.perform(post("/api/v1/salary-revisions/1/apply")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPLIED"));
    }

    @Test
    public void testGetSalaryHistorySuccess() throws Exception {
        mockAuthSuccess();
        SalaryRevisionResponse revision = new SalaryRevisionResponse();
        revision.setId(1L);
        revision.setNewSalary(BigDecimal.valueOf(5000));

        when(appraisalService.getSalaryRevisions(1L)).thenReturn(List.of(revision));

        mockMvc.perform(get("/api/v1/employees/1/salary-history")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    public void testGetIncrementLetterSuccess() throws Exception {
        mockAuthSuccess();
        IncrementLetterResponse letter = new IncrementLetterResponse();
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
}
