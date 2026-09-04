package com.example.ems.asset;

import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.*;
import com.example.ems.asset.service.AssetHistoryService;
import com.example.ems.asset.service.AssetLifecycleService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AssetWorkflowIntegrationTest {

    @Autowired
    private com.example.ems.organization.repository.OrganizationRepository organizationRepository;

    @Autowired
    private AssetLifecycleService lifecycleService;

    @Autowired
    private AssetHistoryService historyService;

    @Autowired
    private AssetCategoryRepository categoryRepository;

    @Autowired
    private AssetLocationRepository locationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetAssignmentRepository assignmentRepository;

    private com.example.ems.organization.entity.Organization org;
    private AssetCategory category;
    private AssetLocation loc1;
    private AssetLocation loc2;
    private Employee emp1;
    private Employee emp2;
    private Employee inactiveEmp;
    private String adminEmail = "asset.admin@company.com";

    @BeforeEach
    public void setUp() {
        org = new com.example.ems.organization.entity.Organization();
        org.setName("Asset Test Organization");
        org.setOrganizationCode("ORG_AST_" + System.currentTimeMillis());
        org = organizationRepository.save(org);

        category = categoryRepository.save(new AssetCategory(org.getId(), "Laptops & Mobile", "LAP-MOB", "High performance dev machines"));
        loc1 = locationRepository.save(new AssetLocation(org.getId(), "Bangalore HQ - Floor 4", "BLR-F4", null));
        loc2 = locationRepository.save(new AssetLocation(org.getId(), "Hyderabad Office - Floor 2", "HYD-F2", null));

        emp1 = new Employee();
        emp1.setFullName("Kavya Sharma");
        emp1.setEmail("kavya.sharma@company.com");
        emp1.setEmployeeId("EMP-ASSET-01");
        emp1.setOrganization(org);
        emp1.setStatus("ACTIVE");
        emp1 = employeeRepository.save(emp1);

        emp2 = new Employee();
        emp2.setFullName("Vikram Patel");
        emp2.setEmail("vikram.patel@company.com");
        emp2.setEmployeeId("EMP-ASSET-02");
        emp2.setOrganization(org);
        emp2.setStatus("ACTIVE");
        emp2 = employeeRepository.save(emp2);

        inactiveEmp = new Employee();
        inactiveEmp.setFullName("Inactive User");
        inactiveEmp.setEmail("inactive@company.com");
        inactiveEmp.setEmployeeId("EMP-INACTIVE");
        inactiveEmp.setOrganization(org);
        inactiveEmp.setStatus("INACTIVE");
        inactiveEmp = employeeRepository.save(inactiveEmp);
    }

    @Test
    @DisplayName("Full End-to-End Asset Lifecycle Workflow: Create -> Assign -> Transfer -> Return -> Retire -> Verify Audit History")
    public void testFullAssetLifecycleWorkflow() {
        Long orgId = org.getId();

        // Step 1: Create New Asset (Status -> AVAILABLE)
        CreateAssetRequest createReq = new CreateAssetRequest();
        createReq.setAssetCode("AST-MAC-M3-001");
        createReq.setAssetName("MacBook Pro M3 Max 32GB");
        createReq.setCategoryId(category.getId());
        createReq.setLocationId(loc1.getId());
        createReq.setBrand("Apple");
        createReq.setModel("MacBook Pro 16");
        createReq.setSerialNumber("SN-M3-987654");
        createReq.setPurchaseDate(LocalDate.now().minusDays(30));
        createReq.setPurchaseCost(new BigDecimal("3200.00"));
        createReq.setVendor("Apple Authorized Reseller");
        createReq.setDescription("Primary engineering workstation");

        AssetResponse createdAsset = lifecycleService.createAsset(orgId, createReq, adminEmail);

        assertNotNull(createdAsset.getId());
        assertEquals("AST-MAC-M3-001", createdAsset.getAssetCode());
        assertEquals(AssetStatus.AVAILABLE, createdAsset.getStatus());
        assertEquals("MacBook Pro M3 Max 32GB", createdAsset.getAssetName());

        Long assetId = createdAsset.getId();

        // Step 2: Assign Asset to Employee 1 (Kavya Sharma)
        lifecycleService.executeAssignmentInternal(
                orgId,
                assetRepository.findById(assetId).orElseThrow(),
                emp1,
                loc1,
                LocalDate.now().minusDays(15),
                LocalDate.now().plusMonths(6),
                "Assigned for Q3 Cloud Migration Project",
                adminEmail
        );

        AssetResponse assignedAsset = lifecycleService.getAssetById(orgId, assetId);
        assertEquals(AssetStatus.ASSIGNED, assignedAsset.getStatus());

        // Verify active assignment record exists
        AssetAssignment activeAssign = assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE)
                .orElseThrow();
        assertEquals(emp1.getId(), activeAssign.getEmployee().getId());
        assertEquals(AssignmentStatus.ACTIVE, activeAssign.getStatus());

        // Step 3: Transfer Asset from Employee 1 to Employee 2 (Vikram Patel at Hyderabad)
        lifecycleService.executeTransferInternal(
                orgId,
                assetRepository.findById(assetId).orElseThrow(),
                activeAssign,
                emp2,
                loc2,
                LocalDate.now().minusDays(2),
                "Transferred for Hyderabad Onsite Lead Assignment",
                adminEmail
        );

        AssetResponse transferredAsset = lifecycleService.getAssetById(orgId, assetId);
        assertEquals(AssetStatus.ASSIGNED, transferredAsset.getStatus());
        assertEquals(loc2.getId(), transferredAsset.getLocationId());

        // Verify assignment updated to Vikram Patel
        AssetAssignment transferredAssign = assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE)
                .orElseThrow();
        assertEquals(emp2.getId(), transferredAssign.getEmployee().getId());

        // Step 4: Return Asset from Employee 2 with GOOD condition -> Status becomes AVAILABLE
        ReturnAssetRequest returnReq = new ReturnAssetRequest();
        returnReq.setReturnDate(LocalDate.now());
        returnReq.setCondition(AssetCondition.GOOD);
        returnReq.setRemarks("Project completed, returned in good condition");

        AssetResponse returnedAsset = lifecycleService.returnAsset(orgId, assetId, returnReq, adminEmail);
        assertEquals(AssetStatus.AVAILABLE, returnedAsset.getStatus());
        assertTrue(assignmentRepository.findByAssetIdAndStatus(assetId, AssignmentStatus.ACTIVE).isEmpty());

        // Step 5: Retire Asset -> Status becomes RETIRED
        lifecycleService.executeRetireInternal(
                orgId,
                assetRepository.findById(assetId).orElseThrow(),
                "End of lifespan / Hardware refresh policy",
                adminEmail
        );

        AssetResponse retiredAsset = lifecycleService.getAssetById(orgId, assetId);
        assertEquals(AssetStatus.RETIRED, retiredAsset.getStatus());

        // Step 6: Verify Audit History Trajectory
        List<AssetHistoryResponse> history = historyService.getAssetHistory(orgId, assetId);
        assertFalse(history.isEmpty());
        assertTrue(history.size() >= 4);

        assertTrue(history.stream().anyMatch(h -> h.getEventType() == AssetEventType.ASSET_CREATED));
        assertTrue(history.stream().anyMatch(h -> h.getEventType() == AssetEventType.ASSIGNED));
        assertTrue(history.stream().anyMatch(h -> h.getEventType() == AssetEventType.TRANSFERRED));
        assertTrue(history.stream().anyMatch(h -> h.getEventType() == AssetEventType.RETURNED));
        assertTrue(history.stream().anyMatch(h -> h.getEventType() == AssetEventType.RETIRED));
    }

    @Test
    @DisplayName("Validation: Duplicate Asset Code and Serial Number should throw 409 CONFLICT")
    public void testDuplicateAssetCodeAndSerialValidation() {
        Long orgId = org.getId();

        CreateAssetRequest req1 = new CreateAssetRequest();
        req1.setAssetCode("AST-DUP-001");
        req1.setAssetName("Dell XPS 15");
        req1.setCategoryId(category.getId());
        req1.setLocationId(loc1.getId());
        req1.setSerialNumber("SN-UNIQUE-999");
        req1.setPurchaseDate(LocalDate.now());
        req1.setPurchaseCost(new BigDecimal("1500.00"));

        lifecycleService.createAsset(orgId, req1, adminEmail);

        // Try duplicate asset code
        CreateAssetRequest duplicateCodeReq = new CreateAssetRequest();
        duplicateCodeReq.setAssetCode("AST-DUP-001");
        duplicateCodeReq.setAssetName("Dell XPS 15 Copy");
        duplicateCodeReq.setCategoryId(category.getId());
        duplicateCodeReq.setLocationId(loc1.getId());
        duplicateCodeReq.setPurchaseDate(LocalDate.now());
        duplicateCodeReq.setPurchaseCost(new BigDecimal("1500.00"));

        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class, () ->
                lifecycleService.createAsset(orgId, duplicateCodeReq, adminEmail));
        assertEquals(409, ex1.getStatusCode().value());
        assertTrue(ex1.getReason().contains("already exists"));

        // Try duplicate serial number
        CreateAssetRequest duplicateSerialReq = new CreateAssetRequest();
        duplicateSerialReq.setAssetCode("AST-DIFF-002");
        duplicateSerialReq.setAssetName("Dell XPS 15 Diff Code");
        duplicateSerialReq.setCategoryId(category.getId());
        duplicateSerialReq.setLocationId(loc1.getId());
        duplicateSerialReq.setSerialNumber("SN-UNIQUE-999");
        duplicateSerialReq.setPurchaseDate(LocalDate.now());
        duplicateSerialReq.setPurchaseCost(new BigDecimal("1500.00"));

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class, () ->
                lifecycleService.createAsset(orgId, duplicateSerialReq, adminEmail));
        assertEquals(409, ex2.getStatusCode().value());
        assertTrue(ex2.getReason().contains("Serial number"));
    }

    @Test
    @DisplayName("Validation: Inactive Category or Location should throw 400 BAD_REQUEST")
    public void testInactiveCategoryAndLocationValidation() {
        Long orgId = org.getId();

        AssetCategory inactiveCategory = categoryRepository.save(new AssetCategory(orgId, "Inactive Cat", "INACT", "Desc"));
        inactiveCategory.setActive(false);
        categoryRepository.save(inactiveCategory);

        CreateAssetRequest req = new CreateAssetRequest();
        req.setAssetCode("AST-INACT-001");
        req.setAssetName("Test Asset");
        req.setCategoryId(inactiveCategory.getId());
        req.setLocationId(loc1.getId());
        req.setPurchaseDate(LocalDate.now());
        req.setPurchaseCost(new BigDecimal("500.00"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                lifecycleService.createAsset(orgId, req, adminEmail));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("inactive"));
    }

    @Test
    @DisplayName("Validation: Assigning asset to inactive employee should throw 400 BAD_REQUEST")
    public void testInactiveEmployeeAssignmentValidation() {
        Long orgId = org.getId();

        CreateAssetRequest req = new CreateAssetRequest();
        req.setAssetCode("AST-VAL-001");
        req.setAssetName("Lenovo ThinkPad");
        req.setCategoryId(category.getId());
        req.setLocationId(loc1.getId());
        req.setPurchaseDate(LocalDate.now());
        req.setPurchaseCost(new BigDecimal("1200.00"));

        AssetResponse asset = lifecycleService.createAsset(orgId, req, adminEmail);

        AssignAssetRequest assignReq = new AssignAssetRequest();
        assignReq.setEmployeeId(inactiveEmp.getId());
        assignReq.setLocationId(loc1.getId());
        assignReq.setAssignedDate(LocalDate.now());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                lifecycleService.assignAsset(orgId, asset.getId(), assignReq, 101L, adminEmail));
        assertEquals(400, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("EMPLOYEE_INACTIVE"));
    }

    @Test
    @DisplayName("Validation: Returning asset with DAMAGED condition should transition status to IN_MAINTENANCE")
    public void testDamagedConditionReturnTransitionsToMaintenance() {
        Long orgId = org.getId();

        CreateAssetRequest req = new CreateAssetRequest();
        req.setAssetCode("AST-DAM-001");
        req.setAssetName("Monitor 27 Inch");
        req.setCategoryId(category.getId());
        req.setLocationId(loc1.getId());
        req.setPurchaseDate(LocalDate.now());
        req.setPurchaseCost(new BigDecimal("400.00"));

        AssetResponse asset = lifecycleService.createAsset(orgId, req, adminEmail);

        // Assign asset
        lifecycleService.executeAssignmentInternal(
                orgId,
                assetRepository.findById(asset.getId()).orElseThrow(),
                emp1,
                loc1,
                LocalDate.now(),
                null,
                "Assigned monitor",
                adminEmail
        );

        // Return asset with DAMAGED condition
        ReturnAssetRequest returnReq = new ReturnAssetRequest();
        returnReq.setReturnDate(LocalDate.now());
        returnReq.setCondition(AssetCondition.DAMAGED);
        returnReq.setRemarks("Display screen cracked during transit");

        AssetResponse returned = lifecycleService.returnAsset(orgId, asset.getId(), returnReq, adminEmail);
        assertEquals(AssetStatus.IN_MAINTENANCE, returned.getStatus());
        assertEquals(AssetCondition.DAMAGED, returned.getCondition());
    }
}
