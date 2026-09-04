package com.example.ems.asset.service;

import com.example.ems.asset.dto.AssetDtos.*;
import com.example.ems.asset.entity.*;
import com.example.ems.asset.repository.*;
import com.example.ems.employee.entity.Employee;
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
    private AssetService assetService;

    @Mock
    private AssetStateMachineService stateMachineService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

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

        when(assetService.createAsset(eq(orgId), any(CreateAssetRequest.class), anyString()))
                .thenReturn(new AssetResponse(1L, "AST-001", "MacBook Pro 16", 100L, "Laptops", 200L, "HQ Floor 3", null, null, null, LocalDate.now(), new BigDecimal("2000.00"), new BigDecimal("2000.00"), AssetStatus.AVAILABLE, AssetCondition.GOOD, "ACTIVE", null, null, null, false, 0L, null));

        AssetResponse resp = lifecycleService.createAsset(orgId, req, "admin@example.com");

        assertNotNull(resp);
        assertEquals(AssetStatus.AVAILABLE, resp.getStatus());
    }

    @Test
    @DisplayName("Cannot delete ASSIGNED asset")
    void deleteAsset_WhenAssigned_ThrowsConflict() {
        doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Cannot delete an ASSIGNED asset"))
                .when(assetService).deleteAsset(orgId, 1L, "admin@example.com");

        assertThrows(ResponseStatusException.class, () -> lifecycleService.deleteAsset(orgId, 1L, "admin@example.com"));
    }

    @Test
    @DisplayName("Return asset with GOOD condition should set status back to RETURNED")
    void returnAsset_GoodCondition_SetsReturned() {
        sampleAsset.setStatus(AssetStatus.ASSIGNED);
        Employee emp = new Employee();
        emp.setId(50L);

        AssetAssignment activeAssign = new AssetAssignment(orgId, sampleAsset, emp, sampleLocation, LocalDate.now().minusDays(10), null, "Initial assign");
        activeAssign.setId(500L);

        when(assetRepository.findByIdAndOrganizationIdWithLock(1L, orgId)).thenReturn(Optional.of(sampleAsset));
        when(stateMachineService.getNextStatus(any(), any())).thenReturn(AssetStatus.RETURNED);
        when(assignmentRepository.findByAssetIdAndStatus(1L, AssignmentStatus.ACTIVE)).thenReturn(Optional.of(activeAssign));
        when(assetRepository.save(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        ReturnAssetRequest req = new ReturnAssetRequest();
        req.setReturnDate(LocalDate.now());
        req.setCondition(AssetCondition.GOOD);
        req.setRemarks("Returned in pristine condition");

        AssetResponse resp = lifecycleService.returnAsset(orgId, 1L, req, "admin@example.com");

        assertNotNull(resp);
        assertEquals(AssignmentStatus.RETURNED, activeAssign.getStatus());
    }
}
