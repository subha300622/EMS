package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.payroll.dto.SalaryDependencyGraphResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
import com.example.ems.payroll.repository.SalaryStructureRepository;
import com.example.ems.payroll.validation.SalaryValidationErrorType;
import com.example.ems.payroll.validation.SalaryValidationResult;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalaryStructureValidationServiceTest {

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @Mock
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Spy
    private SalaryDependencyGraphService salaryDependencyGraphService = new SalaryDependencyGraphService();

    @InjectMocks
    private SalaryStructureValidationService validationService;

    private final Long orgId = 1L;
    private SalaryStructure structure;
    private SalaryComponent basicComponent;
    private SalaryComponent hraComponent;
    private SalaryComponent travelComponent;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        structure = new SalaryStructure(orgId, "Engineering Standard", "ENG_STD", null, "INR", PayFrequency.MONTHLY, null, null);
        structure.setId(100L);
        structure.setStatus(SalaryStructureStatus.DRAFT);

        basicComponent = new SalaryComponent(orgId, "Basic Pay", "BASIC", null, SalaryComponentType.EARNING, true, true);
        basicComponent.setId(1L);

        hraComponent = new SalaryComponent(orgId, "Housing Allowance", "HOUSING", null, SalaryComponentType.EARNING, true, true);
        hraComponent.setId(2L);

        travelComponent = new SalaryComponent(orgId, "Travel Allowance", "TRAVEL", null, SalaryComponentType.EARNING, true, true);
        travelComponent.setId(3L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Validate Structure - Empty structure fails with STRUCTURE_EMPTY")
    void testValidateStructure_EmptyStructure_Fails() {
        when(salaryStructureRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(100L)).thenReturn(Collections.emptyList());

        SalaryValidationResult result = validationService.validateStructure(100L);

        assertFalse(result.isValid());
        assertEquals(SalaryStructureStatus.DRAFT, result.getStatus());
        assertEquals(1, result.getErrors().size());
        assertEquals(SalaryValidationErrorType.STRUCTURE_EMPTY, result.getErrors().get(0).getErrorType());
    }

    @Test
    @DisplayName("Validate Structure - Missing dependency outside structure fails with DEPENDENCY_NOT_FOUND")
    void testValidateStructure_MissingDependency_Fails() {
        // HRA depends on Basic, but Basic is NOT in the structure components list
        SalaryStructureComponent sscHra = new SalaryStructureComponent(structure, hraComponent, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicComponent, null, BigDecimal.valueOf(25), null, 1);
        sscHra.setId(10L);

        when(salaryStructureRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(100L)).thenReturn(List.of(sscHra));

        SalaryValidationResult result = validationService.validateStructure(100L);

        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertEquals(SalaryValidationErrorType.DEPENDENCY_NOT_FOUND, result.getErrors().get(0).getErrorType());
        assertTrue(result.getErrors().get(0).getMessage().contains("is not included in this salary structure"));
    }

    @Test
    @DisplayName("Validate Structure - Circular dependency fails with CIRCULAR_DEPENDENCY")
    void testValidateStructure_CircularDependency_Fails() {
        // HRA depends on Travel, Travel depends on HRA
        SalaryStructureComponent sscHra = new SalaryStructureComponent(structure, hraComponent, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, travelComponent, null, BigDecimal.valueOf(25), null, 1);
        SalaryStructureComponent sscTravel = new SalaryStructureComponent(structure, travelComponent, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, hraComponent, null, BigDecimal.valueOf(10), null, 2);

        when(salaryStructureRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(100L)).thenReturn(List.of(sscHra, sscTravel));

        SalaryValidationResult result = validationService.validateStructure(100L);

        assertFalse(result.isValid());
        assertEquals(SalaryStructureStatus.DRAFT, result.getStatus());
        assertEquals(1, result.getErrors().size());
        assertEquals(SalaryValidationErrorType.CIRCULAR_DEPENDENCY, result.getErrors().get(0).getErrorType());
        assertTrue(result.getErrors().get(0).getMessage().contains("HOUSING → TRAVEL → HOUSING"));
    }

    @Test
    @DisplayName("Validate Structure - Valid DAG succeeds, updates calculation_order and advances status to VALIDATED")
    void testValidateStructure_Valid_Succeeds() {
        SalaryStructureComponent sscBasic = new SalaryStructureComponent(structure, basicComponent, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(40000), null, null, 1);
        SalaryStructureComponent sscHra = new SalaryStructureComponent(structure, hraComponent, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicComponent, null, BigDecimal.valueOf(25), null, 2);
        SalaryStructureComponent sscTravel = new SalaryStructureComponent(structure, travelComponent, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, hraComponent, null, BigDecimal.valueOf(10), null, 3);

        when(salaryStructureRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(100L)).thenReturn(List.of(sscBasic, sscHra, sscTravel));

        SalaryValidationResult result = validationService.validateStructure(100L);

        assertTrue(result.isValid());
        assertEquals(SalaryStructureStatus.VALIDATED, result.getStatus());
        assertEquals(List.of("BASIC", "HOUSING", "TRAVEL"), result.getCalculationOrder());
        assertEquals(SalaryStructureStatus.VALIDATED, structure.getStatus());
        verify(salaryStructureRepository, atLeastOnce()).save(structure);
    }

    @Test
    @DisplayName("Activate Structure - Valid structure successfully activates")
    void testActivateStructure_Success() {
        SalaryStructureComponent sscBasic = new SalaryStructureComponent(structure, basicComponent, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(40000), null, null, 1);

        when(salaryStructureRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(100L)).thenReturn(List.of(sscBasic));

        SalaryValidationResult result = validationService.activateStructure(100L);

        assertTrue(result.isValid());
        assertEquals(SalaryStructureStatus.ACTIVE, result.getStatus());
        assertEquals(SalaryStructureStatus.ACTIVE, structure.getStatus());
    }

    @Test
    @DisplayName("Activate Structure - If validation fails, activation is rejected")
    void testActivateStructure_ValidationFails_ThrowsException() {
        // Empty structure
        when(salaryStructureRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(100L)).thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () -> validationService.activateStructure(100L));
    }

    @Test
    @DisplayName("Get Dependency Graph - Read-only diagnostic preview")
    void testGetDependencyGraph_Preview() {
        SalaryStructureComponent sscBasic = new SalaryStructureComponent(structure, basicComponent, CalculationType.FIXED, CalculationBaseType.NONE, null, BigDecimal.valueOf(40000), null, null, 1);
        SalaryStructureComponent sscHra = new SalaryStructureComponent(structure, hraComponent, CalculationType.PERCENTAGE, CalculationBaseType.COMPONENT, basicComponent, null, BigDecimal.valueOf(25), null, 2);

        when(salaryStructureRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(structure));
        when(salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(100L)).thenReturn(List.of(sscBasic, sscHra));

        SalaryDependencyGraphResponse response = validationService.getDependencyGraph(100L);

        assertNotNull(response);
        assertFalse(response.isHasCycle());
        assertEquals(List.of("BASIC", "HOUSING"), response.getCalculationOrder());
        assertEquals(2, response.getComponents().size());
        assertEquals("HOUSING", response.getComponents().get(1).getComponentCode());
        assertEquals(List.of("BASIC"), response.getComponents().get(1).getDependsOn());
    }
}
