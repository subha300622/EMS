package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.*;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetLifecycleServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetCategoryRepository categoryRepository;

    @Mock
    private AssetLocationRepository locationRepository;

    @Mock
    private AssetAssignmentRepository assignmentRepository;

    @Mock
    private AssetActionRequestRepository actionRequestRepository;

    @Mock
    private AssetHistoryService historyService;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AssetLifecycleService lifecycleService;

    private Long orgId = 1L;
    private Asset sampleAsset;
    private AssetCategory sampleCategory;
    private AssetLocation sampleLocation;

    @BeforeEach
    void setUp() {
        sampleCategory = new AssetCategory(orgId, "Laptops", "LAP", "Laptops");
        sampleCategory.setId(100L);

        sampleLocation = new AssetLocation(orgId, "HQ Floor 3", "HQ-3", null);
        sampleLocation.setId(200L);

        sampleAsset = new Asset();
        sampleAsset.setId(1L);
        sampleAsset.setOrganizationId(orgId);
        sampleAsset.setAssetCode("AST-001");
        sampleAsset.setAssetName("MacBook Pro 16");
        sampleAsset.setCategory(sampleCategory);
        sampleAsset.setLocation(sampleLocation);
        sampleAsset.setStatus(AssetStatus.AVAILABLE);
        sampleAsset.setPurchaseDate(LocalDate.now().minusMonths(2));
        sampleAsset.setPurchaseCost(new BigDecimal("2500.00"));
    }

    @Test
    @DisplayName("Create asset should automatically initialize status to AVAILABLE")
    void createAsset_StatusAutoAvailable() {
        CreateAssetRequest req = new CreateAssetRequest();
        req.setAssetCode("AST-001");
        req.setAssetName("MacBook Pro 16");
        req.setCategoryId(100L);
        req.setLocationId(200L);
        req.setPurchaseDate(LocalDate.now().minusDays(5));
        req.setPurchaseCost(new BigDecimal("2000.00"));

        when(assetRepository.existsByOrganizationIdAndAssetCodeIgnoreCase(orgId, "AST-001")).thenReturn(false);
        when(categoryRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(sampleCategory));
        when(locationRepository.findByIdAndOrganizationId(200L, orgId)).thenReturn(Optional.of(sampleLocation));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> {
            Asset a = i.getArgument(0);
            a.setId(1L);
            return a;
        });

        AssetResponse resp = lifecycleService.createAsset(orgId, req, "admin@example.com");

        assertNotNull(resp);
        assertEquals(AssetStatus.AVAILABLE, resp.getStatus());
        verify(historyService).recordHistory(eq(orgId), eq(1L), eq(AssetEventType.ASSET_CREATED), any(), eq("AVAILABLE"), any(), any(), any(), eq(200L), eq("admin@example.com"), any(), any());
    }

    @Test
    @DisplayName("Cannot delete ASSIGNED asset")
    void deleteAsset_WhenAssigned_ThrowsConflict() {
        sampleAsset.setStatus(AssetStatus.ASSIGNED);
        when(assetRepository.findByIdAndOrganizationIdWithLock(1L, orgId)).thenReturn(Optional.of(sampleAsset));

        assertThrows(ResponseStatusException.class, () -> lifecycleService.deleteAsset(orgId, 1L, "admin@example.com"));
    }

    @Test
    @DisplayName("Return asset with GOOD condition should set status back to AVAILABLE")
    void returnAsset_GoodCondition_SetsAvailable() {
        sampleAsset.setStatus(AssetStatus.ASSIGNED);
        Employee emp = new Employee();
        emp.setId(50L);

        AssetAssignment activeAssign = new AssetAssignment(orgId, sampleAsset, emp, sampleLocation, LocalDate.now().minusDays(10), null, "Initial assign");
        activeAssign.setId(500L);

        when(assetRepository.findByIdAndOrganizationIdWithLock(1L, orgId)).thenReturn(Optional.of(sampleAsset));
        when(assignmentRepository.findByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(Optional.of(activeAssign));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        ReturnAssetRequest req = new ReturnAssetRequest();
        req.setReturnDate(LocalDate.now());
        req.setCondition(AssetCondition.GOOD);
        req.setRemarks("Returned in pristine condition");

        AssetResponse resp = lifecycleService.returnAsset(orgId, 1L, req, "admin@example.com");

        assertEquals(AssetStatus.AVAILABLE, resp.getStatus());
        assertEquals(AssignmentStatus.RETURNED, activeAssign.getStatus());
    }
}
