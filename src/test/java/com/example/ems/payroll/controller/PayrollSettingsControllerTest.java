package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.payroll.entity.PayrollSetting;
import com.example.ems.payroll.entity.SalaryComponent;
import com.example.ems.payroll.entity.TaxSlab;
import com.example.ems.payroll.repository.PayrollSettingRepository;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.payroll.repository.TaxSlabRepository;
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

public class PayrollSettingsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PayrollSettingRepository payrollSettingRepository;

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @Mock
    private TaxSlabRepository taxSlabRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private PayrollSettingsController payrollSettingsController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "finance@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(payrollSettingsController).build();

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
    public void testGetParametersSuccess() throws Exception {
        mockPermission("payroll-settings.manage", true);
        PayrollSetting setting = new PayrollSetting("paycycle_start_day", "1", "Start day");
        when(payrollSettingRepository.findAll()).thenReturn(List.of(setting));

        mockMvc.perform(get("/api/v1/payroll-settings/parameters")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].settingKey").value("paycycle_start_day"));
    }

    @Test
    public void testGetComponentsSuccess() throws Exception {
        mockPermission("payroll-settings.manage", true);
        SalaryComponent component = new SalaryComponent("Basic Salary", "EARNING", BigDecimal.valueOf(50.0), null, true);
        when(salaryComponentRepository.findAll()).thenReturn(List.of(component));

        mockMvc.perform(get("/api/v1/payroll-settings/components")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("Basic Salary"));
    }

    @Test
    public void testGetTaxSlabsSuccess() throws Exception {
        mockPermission("payroll-settings.manage", true);
        TaxSlab slab = new TaxSlab("NEW", BigDecimal.valueOf(0.0), BigDecimal.valueOf(300000.0), BigDecimal.ZERO);
        when(taxSlabRepository.findAll()).thenReturn(List.of(slab));

        mockMvc.perform(get("/api/v1/payroll-settings/tax-slabs")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].regime").value("NEW"));
    }

    @Test
    public void testCreateTaxSlabSuccess() throws Exception {
        mockPermission("payroll-settings.manage", true);
        TaxSlab request = new TaxSlab("NEW", BigDecimal.valueOf(600000.0), null, BigDecimal.valueOf(10.0));
        when(taxSlabRepository.save(any(TaxSlab.class))).thenReturn(request);

        mockMvc.perform(post("/api/v1/payroll-settings/tax-slabs")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.regime").value("NEW"));
    }
}
