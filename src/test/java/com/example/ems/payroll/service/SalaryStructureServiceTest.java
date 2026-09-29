package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.payroll.dto.SalaryStructureCreateRequest;
import com.example.ems.payroll.dto.SalaryStructureResponse;
import com.example.ems.payroll.dto.SalaryStructureUpdateRequest;
import com.example.ems.payroll.entity.PayFrequency;
import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.entity.SalaryStructureStatus;
import com.example.ems.payroll.repository.SalaryStructureRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.ems.payroll.repository.SalaryStructureComponentRepository;

@ExtendWith(MockitoExtension.class)
class SalaryStructureServiceTest {

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @Mock
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @InjectMocks
    private SalaryStructureService salaryStructureService;

    private final Long orgId = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Create Structure - Creates version 1 with DRAFT status and normalized code")
    void testCreateStructure_Success() {
        SalaryStructureCreateRequest request = new SalaryStructureCreateRequest(
                "Senior Developer Structure",
                "senior_dev",
                "Structure for senior developers",
                "INR",
                PayFrequency.MONTHLY,
                LocalDate.of(2026, 9, 1),
                null
        );

        when(salaryStructureRepository.existsByOrganizationIdAndCodeAndVersion(orgId, "SENIOR_DEV", 1)).thenReturn(false);
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(inv -> {
            SalaryStructure s = inv.getArgument(0);
            s.setId(101L);
            return s;
        });

        SalaryStructureResponse response = salaryStructureService.createStructure(request);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals("SENIOR_DEV", response.getCode());
        assertEquals("Senior Developer Structure", response.getName());
        assertEquals(1, response.getVersion());
        assertEquals(SalaryStructureStatus.DRAFT, response.getStatus());
        assertEquals(PayFrequency.MONTHLY, response.getPayFrequency());
        assertEquals("INR", response.getCurrency());
        verify(salaryStructureRepository).save(any(SalaryStructure.class));
    }

    @Test
    @DisplayName("Create Structure - Duplicate code and version 1 throws ConflictException")
    void testCreateStructure_Duplicate_ThrowsConflictException() {
        SalaryStructureCreateRequest request = new SalaryStructureCreateRequest(
                "Senior Developer Structure",
                "SENIOR_DEV",
                null,
                "INR",
                PayFrequency.MONTHLY,
                null,
                null
        );

        when(salaryStructureRepository.existsByOrganizationIdAndCodeAndVersion(orgId, "SENIOR_DEV", 1)).thenReturn(true);

        assertThrows(ConflictException.class, () -> salaryStructureService.createStructure(request));
        verify(salaryStructureRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update Structure - In DRAFT status updates successfully")
    void testUpdateStructure_Draft_Success() {
        SalaryStructure structure = new SalaryStructure(orgId, "Old Name", "DEV", "Old desc", "INR", PayFrequency.MONTHLY, null, null);
        structure.setId(20L);
        structure.setStatus(SalaryStructureStatus.DRAFT);

        when(salaryStructureRepository.findByIdAndOrganizationId(20L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(inv -> inv.getArgument(0));

        SalaryStructureUpdateRequest updateRequest = new SalaryStructureUpdateRequest(
                "Updated Developer Structure",
                "Updated description",
                "USD",
                PayFrequency.BIWEEKLY,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
        );

        SalaryStructureResponse response = salaryStructureService.updateStructure(20L, updateRequest);

        assertEquals("Updated Developer Structure", response.getName());
        assertEquals("USD", response.getCurrency());
        assertEquals(PayFrequency.BIWEEKLY, response.getPayFrequency());
        assertEquals(SalaryStructureStatus.DRAFT, response.getStatus());
    }

    @Test
    @DisplayName("Update Structure - In ACTIVE status throws ConflictException to protect historical data")
    void testUpdateStructure_Active_ThrowsConflictException() {
        SalaryStructure structure = new SalaryStructure(orgId, "Active Structure", "ACTIVE_CODE", null, "INR", PayFrequency.MONTHLY, null, null);
        structure.setId(30L);
        structure.setStatus(SalaryStructureStatus.ACTIVE);

        when(salaryStructureRepository.findByIdAndOrganizationId(30L, orgId)).thenReturn(Optional.of(structure));

        SalaryStructureUpdateRequest updateRequest = new SalaryStructureUpdateRequest("New Name", null, null, null, null, null);

        assertThrows(ConflictException.class, () -> salaryStructureService.updateStructure(30L, updateRequest));
        verify(salaryStructureRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create New Version - Branches from active structure to version 2 in DRAFT status")
    void testCreateNewVersion_Success() {
        SalaryStructure v1 = new SalaryStructure(orgId, "Senior Dev", "SENIOR_DEV", "v1 desc", "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 8, 31));
        v1.setId(10L);
        v1.setVersion(1);
        v1.setStatus(SalaryStructureStatus.ACTIVE);

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(v1));
        when(salaryStructureRepository.findTopByOrganizationIdAndCodeOrderByVersionDesc(orgId, "SENIOR_DEV")).thenReturn(Optional.of(v1));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(inv -> {
            SalaryStructure s = inv.getArgument(0);
            s.setId(11L);
            return s;
        });

        SalaryStructureCreateRequest override = new SalaryStructureCreateRequest(
                "Senior Dev Revised",
                "SENIOR_DEV",
                "v2 desc",
                "INR",
                PayFrequency.MONTHLY,
                LocalDate.of(2026, 9, 1),
                null
        );

        SalaryStructureResponse response = salaryStructureService.createNewVersion(10L, override);

        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertEquals(2, response.getVersion());
        assertEquals(SalaryStructureStatus.DRAFT, response.getStatus());
        assertEquals("Senior Dev Revised", response.getName());
        assertEquals(LocalDate.of(2026, 9, 1), response.getEffectiveFrom());
    }

    @Test
    @DisplayName("Lifecycle Transitions: DRAFT -> VALIDATED -> ACTIVE -> INACTIVE")
    void testLifecycleTransitions() {
        SalaryStructure structure = new SalaryStructure(orgId, "QA Structure", "QA_STRUCT", null, "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 1, 1), null);
        structure.setId(40L);
        structure.setStatus(SalaryStructureStatus.DRAFT);

        when(salaryStructureRepository.findByIdAndOrganizationId(40L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureRepository.save(any(SalaryStructure.class))).thenAnswer(inv -> inv.getArgument(0));

        // 1. Validate
        SalaryStructureResponse validated = salaryStructureService.validateStructure(40L);
        assertEquals(SalaryStructureStatus.VALIDATED, validated.getStatus());

        // 2. Activate
        SalaryStructureResponse activated = salaryStructureService.activateStructure(40L);
        assertEquals(SalaryStructureStatus.ACTIVE, activated.getStatus());

        // 3. Deactivate
        SalaryStructureResponse deactivated = salaryStructureService.deactivateStructure(40L);
        assertEquals(SalaryStructureStatus.INACTIVE, deactivated.getStatus());
    }

    @Test
    @DisplayName("Validate Structure - Invalid date range throws BadRequestException")
    void testValidateStructure_InvalidDates_ThrowsBadRequestException() {
        SalaryStructure structure = new SalaryStructure(orgId, "Invalid Dates", "INV_DATES", null, "INR", PayFrequency.MONTHLY, LocalDate.of(2026, 12, 31), LocalDate.of(2026, 1, 1));
        structure.setId(50L);
        structure.setStatus(SalaryStructureStatus.DRAFT);

        when(salaryStructureRepository.findByIdAndOrganizationId(50L, orgId)).thenReturn(Optional.of(structure));

        assertThrows(BadRequestException.class, () -> salaryStructureService.validateStructure(50L));
    }
}
