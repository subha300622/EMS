package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDto;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.repository.MyAssetRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AssetAdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MyAssetRepository myAssetRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AssetAdminController assetAdminController;

    private static final String TOKEN = "mock-token";
    private static final String AUTH_HEADER = "Bearer " + TOKEN;
    private static final String EMAIL = "admin@example.com";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(assetAdminController).build();

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
    public void testGetAllAssetsSuccess() throws Exception {
        mockPermission("asset.manage", true);
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setAssetName("Test Laptop");
        when(myAssetRepository.findAll()).thenReturn(List.of(asset));

        mockMvc.perform(get("/api/v1/assets")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].assetName").value("Test Laptop"));
    }

    @Test
    public void testCreateAssetSuccess() throws Exception {
        mockPermission("asset.manage", true);
        AssetDto dto = new AssetDto();
        dto.setAssetCode("AST001");
        dto.setAssetName("New Laptop");
        dto.setCategory("LAPTOP");
        dto.setBrand("Dell");
        dto.setModel("Latitude");
        dto.setSerialNumber("SN12345");
        dto.setPurchasePrice(BigDecimal.valueOf(1200.00));
        dto.setCurrentValue(BigDecimal.valueOf(1200.00));

        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setAssetName("New Laptop");
        when(myAssetRepository.save(any(MyAsset.class))).thenReturn(asset);

        mockMvc.perform(post("/api/v1/assets")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.assetName").value("New Laptop"));
    }

    @Test
    public void testAssignAssetSuccess() throws Exception {
        mockPermission("asset.manage", true);
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        Employee emp = new Employee();
        emp.setId(10L);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(emp));

        when(myAssetRepository.save(any(MyAsset.class))).thenReturn(asset);

        mockMvc.perform(post("/api/v1/assets/1/assign")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(Map.of("employeeId", 10L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testReturnAssetSuccess() throws Exception {
        mockPermission("asset.manage", true);
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(myAssetRepository.save(any(MyAsset.class))).thenReturn(asset);

        mockMvc.perform(post("/api/v1/assets/1/return")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testDisposeAssetSuccess() throws Exception {
        mockPermission("asset.manage", true);
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(myAssetRepository.save(any(MyAsset.class))).thenReturn(asset);

        mockMvc.perform(post("/api/v1/assets/1/dispose")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
