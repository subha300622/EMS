package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.payroll.dto.StructureComponentCreateRequest;
import com.example.ems.payroll.dto.StructureComponentResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryStructureComponentServiceTest {

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @Mock
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @InjectMocks
    private SalaryStructureComponentService salaryStructureComponentService;

    private final Long orgId = 1L;
    private SalaryStructure draftStructure;
    private SalaryComponent basicComponent;
    private SalaryComponent hraComponent;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        draftStructure = new SalaryStructure(orgId, "Dev Structure", "DEV_STRUCT", null, "INR", PayFrequency.MONTHLY, null, null);
        draftStructure.setId(10L);
        draftStructure.setStatus(SalaryStructureStatus.DRAFT);

        basicComponent = new SalaryComponent(orgId, "Basic Pay", "BASIC_PAY", null, SalaryComponentType.EARNING, true, true);
        basicComponent.setId(1L);

        hraComponent = new SalaryComponent(orgId, "Housing Allowance", "HRA", null, SalaryComponentType.EARNING, true, true);
        hraComponent.setId(2L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Add Component - FIXED calculation type adds successfully")
    void testAddComponent_Fixed_Success() {
        StructureComponentCreateRequest request = new StructureComponentCreateRequest(
                1L,
                CalculationType.FIXED,
                CalculationBaseType.NONE,
                null,
                BigDecimal.valueOf(30000),
                null,
                null,
                1
        );

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(draftStructure));
        when(salaryComponentRepository.findByIdAndOrganizationId(1L, orgId)).thenReturn(Optional.of(basicComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(10L, 1L)).thenReturn(false);
        when(salaryStructureComponentRepository.save(any(SalaryStructureComponent.class))).thenAnswer(inv -> {
            SalaryStructureComponent ssc = inv.getArgument(0);
            ssc.setId(1001L);
            return ssc;
        });

        StructureComponentResponse response = salaryStructureComponentService.addComponentToStructure(10L, request);

        assertNotNull(response);
        assertEquals(1001L, response.getId());
        assertEquals(1L, response.getComponentId());
        assertEquals("BASIC_PAY", response.getComponentCode());
        assertEquals(CalculationType.FIXED, response.getCalculationType());
        assertEquals(BigDecimal.valueOf(30000), response.getFixedAmount());
        assertEquals(1, response.getCalculationOrder());
    }

    @Test
    @DisplayName("Add Component - PERCENTAGE of COMPONENT adds successfully")
    void testAddComponent_PercentageOfComponent_Success() {
        StructureComponentCreateRequest request = new StructureComponentCreateRequest(
                2L,
                CalculationType.PERCENTAGE,
                CalculationBaseType.COMPONENT,
                1L,
                null,
                BigDecimal.valueOf(25.0),
                null,
                2
        );

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(draftStructure));
        when(salaryComponentRepository.findByIdAndOrganizationId(2L, orgId)).thenReturn(Optional.of(hraComponent));
        when(salaryComponentRepository.findByIdAndOrganizationId(1L, orgId)).thenReturn(Optional.of(basicComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(10L, 2L)).thenReturn(false);
        when(salaryStructureComponentRepository.save(any(SalaryStructureComponent.class))).thenAnswer(inv -> {
            SalaryStructureComponent ssc = inv.getArgument(0);
            ssc.setId(1002L);
            return ssc;
        });

        StructureComponentResponse response = salaryStructureComponentService.addComponentToStructure(10L, request);

        assertNotNull(response);
        assertEquals(1002L, response.getId());
        assertEquals(CalculationType.PERCENTAGE, response.getCalculationType());
        assertEquals(CalculationBaseType.COMPONENT, response.getCalculationBaseType());
        assertEquals(1L, response.getCalculationBaseComponentId());
        assertEquals(BigDecimal.valueOf(25.0), response.getPercentage());
    }

    @Test
    @DisplayName("Add Component - Self-referencing base component throws BadRequestException")
    void testAddComponent_SelfReferencingBase_ThrowsBadRequestException() {
        StructureComponentCreateRequest request = new StructureComponentCreateRequest(
                1L,
                CalculationType.PERCENTAGE,
                CalculationBaseType.COMPONENT,
                1L, // self base
                null,
                BigDecimal.valueOf(20.0),
                null,
                1
        );

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(draftStructure));
        when(salaryComponentRepository.findByIdAndOrganizationId(1L, orgId)).thenReturn(Optional.of(basicComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(10L, 1L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> salaryStructureComponentService.addComponentToStructure(10L, request));
    }

    @Test
    @DisplayName("Add Component - Mutating an ACTIVE structure throws ConflictException")
    void testAddComponent_ActiveStructure_ThrowsConflictException() {
        draftStructure.setStatus(SalaryStructureStatus.ACTIVE);

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(draftStructure));

        StructureComponentCreateRequest request = new StructureComponentCreateRequest(
                1L, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(5000), null, null, 1
        );

        assertThrows(ConflictException.class, () -> salaryStructureComponentService.addComponentToStructure(10L, request));
    }

    @Test
    @DisplayName("Add Component - Adding to a VALIDATED structure resets status to DRAFT")
    void testAddComponent_ValidatedStructure_ResetsToDraft() {
        draftStructure.setStatus(SalaryStructureStatus.VALIDATED);

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(draftStructure));
        when(salaryComponentRepository.findByIdAndOrganizationId(1L, orgId)).thenReturn(Optional.of(basicComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(10L, 1L)).thenReturn(false);
        when(salaryStructureComponentRepository.save(any(SalaryStructureComponent.class))).thenAnswer(inv -> inv.getArgument(0));

        StructureComponentCreateRequest request = new StructureComponentCreateRequest(
                1L, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(5000), null, null, 1
        );

        salaryStructureComponentService.addComponentToStructure(10L, request);

        assertEquals(SalaryStructureStatus.DRAFT, draftStructure.getStatus());
        verify(salaryStructureRepository).save(draftStructure);
    }

    @Test
    @DisplayName("Get Structure Components - Retrieves list sorted by calculation order")
    void testGetStructureComponents() {
        SalaryStructureComponent ssc1 = new SalaryStructureComponent(draftStructure, basicComponent, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(30000), null, null, 1);
        ssc1.setId(1001L);

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(draftStructure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(10L)).thenReturn(List.of(ssc1));

        List<StructureComponentResponse> components = salaryStructureComponentService.getStructureComponents(10L);

        assertEquals(1, components.size());
        assertEquals("BASIC_PAY", components.get(0).getComponentCode());
    }

    @Test
    @DisplayName("Remove Component - Deletes mapping successfully and resets VALIDATED to DRAFT")
    void testRemoveComponent_Success() {
        draftStructure.setStatus(SalaryStructureStatus.VALIDATED);

        SalaryStructureComponent ssc1 = new SalaryStructureComponent(draftStructure, basicComponent, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(30000), null, null, 1);
        ssc1.setId(501L);

        when(salaryStructureRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(draftStructure));
        when(salaryStructureComponentRepository.findByIdAndSalaryStructureId(501L, 10L)).thenReturn(Optional.of(ssc1));

        salaryStructureComponentService.removeComponentFromStructure(10L, 501L);

        verify(salaryStructureComponentRepository).delete(ssc1);
        assertEquals(SalaryStructureStatus.DRAFT, draftStructure.getStatus());
    }
}
