package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDto;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.*;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

    @Mock
    private MyAssetAssignmentRepository myAssetAssignmentRepository;

    @Mock
    private MyAssetMaintenanceRepository myAssetMaintenanceRepository;

    @Mock
    private MyAssetDocumentRepository myAssetDocumentRepository;

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
        
        // Default permission granted for administrator
        when(roleService.hasPermission(EMAIL, "asset.manage")).thenReturn(true);
    }


    @Test
    public void testGetAssetDashboard() throws Exception {
        MyAsset asset1 = new MyAsset();
        asset1.setStatus("ASSIGNED");
        asset1.setCurrentValue(BigDecimal.valueOf(1000));

        MyAsset asset2 = new MyAsset();
        asset2.setStatus("AVAILABLE");
        asset2.setCurrentValue(BigDecimal.valueOf(500));

        when(myAssetRepository.findAll()).thenReturn(List.of(asset1, asset2));

        mockMvc.perform(get("/api/v1/assets/dashboard")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAssets").value(2))
                .andExpect(jsonPath("$.data.assignedAssets").value(1))
                .andExpect(jsonPath("$.data.availableAssets").value(1))
                .andExpect(jsonPath("$.data.totalValue").value(1500));
    }

    @Test
    public void testGetAllAssetsSuccess() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setAssetName("Test Laptop");
        asset.setAssetCode("AST001");
        asset.setStatus("AVAILABLE");
        
        Page<MyAsset> page = new PageImpl<>(List.of(asset), PageRequest.of(0, 10), 1);
        when(myAssetRepository.findFiltered(any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/assets")
                .header("Authorization", AUTH_HEADER)
                .param("search", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Laptop"));
    }

    @Test
    public void testGetAssetByIdSuccess() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setAssetName("Dell XPS");
        asset.setAssetCode("AST-002");
        asset.setStatus("AVAILABLE");
        
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        mockMvc.perform(get("/api/v1/assets/1")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Dell XPS"));
    }

    @Test
    public void testCreateAssetSuccess() throws Exception {
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
        asset.setAssetCode("AST001");
        asset.setAssetName("New Laptop");
        asset.setStatus("AVAILABLE");
        
        when(myAssetRepository.save(any(MyAsset.class))).thenReturn(asset);

        mockMvc.perform(post("/api/v1/assets")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("New Laptop"));
    }

    @Test
    public void testAssignAssetSuccess() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setStatus("AVAILABLE");
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
    public void testTransferAssetSuccess() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setStatus("ASSIGNED");
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        Employee toEmp = new Employee();
        toEmp.setId(20L);
        when(employeeRepository.findById(20L)).thenReturn(Optional.of(toEmp));

        mockMvc.perform(post("/api/v1/assets/1/transfer")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(Map.of("toEmployeeId", 20L, "remarks", "transfer remarks"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    public void testReturnAssetSuccess() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setStatus("ASSIGNED");
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(myAssetRepository.save(any(MyAsset.class))).thenReturn(asset);

        mockMvc.perform(post("/api/v1/assets/1/return")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));
    }

    @Test
    public void testGetMaintenanceRecords() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);

        MyAssetMaintenance maint = new MyAssetMaintenance(asset, "Battery replacement", "Dell", BigDecimal.valueOf(5000));
        maint.setId(101L);

        when(myAssetMaintenanceRepository.findByAssetIdOrderByStartDateDesc(1L)).thenReturn(List.of(maint));

        mockMvc.perform(get("/api/v1/assets/1/maintenance")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].issue").value("Battery replacement"));
    }

    @Test
    public void testCreateMaintenanceRequest() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        mockMvc.perform(post("/api/v1/assets/1/maintenance")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(Map.of("issue", "Screen broken", "vendor", "Dell Services", "estimatedCost", 8000))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.issue").value("Screen broken"));
    }

    @Test
    public void testCompleteMaintenance() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);

        MyAssetMaintenance maint = new MyAssetMaintenance(asset, "Screen broken", "Dell Services", BigDecimal.valueOf(8000));
        maint.setId(201L);
        when(myAssetMaintenanceRepository.findById(201L)).thenReturn(Optional.of(maint));

        mockMvc.perform(patch("/api/v1/assets/maintenance/201/complete")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(Map.of("actualCost", 7500))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    public void testUploadAssetDocument() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        MockMultipartFile file = new MockMultipartFile("file", "invoice.pdf", "application/pdf", "dummy data".getBytes());

        mockMvc.perform(multipart("/api/v1/assets/1/documents")
                .file(file)
                .param("documentType", "Invoice")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").value("invoice.pdf"))
                .andExpect(jsonPath("$.data.documentType").value("Invoice"));
    }

    @Test
    public void testGetAssetDocuments() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);

        MyAssetDocument doc = new MyAssetDocument(asset, "warranty.pdf", "application/pdf", "dummy bytes".getBytes(), "Warranty Card");
        doc.setId(301L);

        when(myAssetDocumentRepository.findByAssetIdOrderByUploadedAtDesc(1L)).thenReturn(List.of(doc));

        mockMvc.perform(get("/api/v1/assets/1/documents")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fileName").value("warranty.pdf"));
    }

    @Test
    public void testUpdateAssetStatusSuccess() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setStatus("AVAILABLE");
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(myAssetRepository.save(any(MyAsset.class))).thenReturn(asset);

        mockMvc.perform(patch("/api/v1/assets/1/status")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RETIRED\",\"remarks\":\"obsolete\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RETIRED"));
    }

    @Test
    public void testUpdateAssetStatusInvalidStatus() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        mockMvc.perform(patch("/api/v1/assets/1/status")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testUpdateAssetStatusConflictFromDisposed() throws Exception {
        MyAsset asset = new MyAsset();
        asset.setId(1L);
        asset.setStatus("DISPOSED");
        when(myAssetRepository.findById(1L)).thenReturn(Optional.of(asset));

        mockMvc.perform(patch("/api/v1/assets/1/status")
                .header("Authorization", AUTH_HEADER)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"AVAILABLE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    public void testReports() throws Exception {
        MyAsset asset1 = new MyAsset();
        asset1.setStatus("ASSIGNED");
        asset1.setCategory("LAPTOP");
        asset1.setPurchasePrice(BigDecimal.valueOf(1000));
        asset1.setCurrentValue(BigDecimal.valueOf(900));

        when(myAssetRepository.findAll()).thenReturn(List.of(asset1));

        mockMvc.perform(get("/api/v1/assets/reports/utilization")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAssets").value(1));

        mockMvc.perform(get("/api/v1/assets/reports/depreciation")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalDepreciation").value(100));

        mockMvc.perform(get("/api/v1/assets/reports/inventory")
                .header("Authorization", AUTH_HEADER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAssets").value(1));
    }
}
