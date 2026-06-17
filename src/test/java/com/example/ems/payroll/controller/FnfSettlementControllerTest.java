package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.payroll.entity.FnfSettlement;
import com.example.ems.payroll.service.FnfSettlementService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FnfSettlementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FnfSettlementService fnfSettlementService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FnfSettlementController fnfSettlementController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "finance@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(fnfSettlementController).build();

        when(jwtService.validateAccessToken(TOKEN)).thenReturn(true);
        when(jwtService.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        User user = new User();
        user.setWorkEmail(EMAIL);
        when(userRepository.findByWorkEmail(EMAIL)).thenReturn(Optional.of(user));
    }

    private void mockPermission(String permission, boolean allowed) {
        when(roleService.hasPermission(EMAIL, permission)).thenReturn(allowed);
    }

    @Test
    public void testGetAllSettlementsSuccess() throws Exception {
        mockPermission("fnf.manage", true);
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(1L);
        settlement.setEmployeeId(10L);
        settlement.setNetAmount(BigDecimal.valueOf(5000.00));
        when(fnfSettlementService.getAllSettlements()).thenReturn(List.of(settlement));

        mockMvc.perform(get("/api/v1/fnf-settlements")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].netAmount").value(5000.00));
    }

    @Test
    public void testCreateSettlementSuccess() throws Exception {
        mockPermission("fnf.manage", true);
        FnfSettlement request = new FnfSettlement();
        request.setEmployeeId(10L);
        request.setGratuity(BigDecimal.valueOf(1000.00));
        request.setNoticePay(BigDecimal.valueOf(2000.00));
        request.setUnpaidSalary(BigDecimal.valueOf(2500.00));
        request.setOtherDeductions(BigDecimal.valueOf(500.00));

        FnfSettlement saved = new FnfSettlement();
        saved.setId(1L);
        saved.setEmployeeId(10L);
        saved.setNetAmount(BigDecimal.valueOf(5000.00));
        when(fnfSettlementService.createSettlement(any(FnfSettlement.class))).thenReturn(saved);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        mockMvc.perform(post("/api/v1/fnf-settlements")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.netAmount").value(5000.00));
    }

    @Test
    public void testApproveSettlementSuccess() throws Exception {
        mockPermission("fnf.manage", true);
        FnfSettlement settlement = new FnfSettlement();
        settlement.setId(1L);
        settlement.setStatus("APPROVED");
        when(fnfSettlementService.approveSettlement(1L)).thenReturn(Optional.of(settlement));

        mockMvc.perform(post("/api/v1/fnf-settlements/1/approve")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }
}
