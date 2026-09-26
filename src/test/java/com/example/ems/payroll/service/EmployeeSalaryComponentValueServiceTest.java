package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.payroll.dto.EmployeeSalaryComponentValueRequest;
import com.example.ems.payroll.dto.EmployeeSalaryComponentValueResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.EmployeeSalaryComponentValueRepository;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSalaryComponentValueServiceTest {

    @Mock
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    @Mock
    private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @Mock
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @InjectMocks
    private EmployeeSalaryComponentValueService componentValueService;

    private final Long orgId = 1L;
    private Employee employee;
    private SalaryStructure activeStructure;
    private EmployeeSalaryAssignment assignment;
    private SalaryComponent basicComponent;
    private SalaryComponent hraComponent;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        employee = new Employee();
        employee.setId(100L);

        activeStructure = new SalaryStructure(orgId, "Engineering Standard", "ENG_STD", null, "INR", PayFrequency.MONTHLY, null, null);
        activeStructure.setId(50L);

        assignment = new EmployeeSalaryAssignment(orgId, employee, activeStructure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Standard");
        assignment.setId(10L);

        basicComponent = new SalaryComponent(orgId, "Basic Pay", "BASIC", null, SalaryComponentType.EARNING, true, true);
        basicComponent.setId(1L);

        hraComponent = new SalaryComponent(orgId, "Housing Allowance", "HOUSING", null, SalaryComponentType.EARNING, true, true);
        hraComponent.setId(2L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Add Component Value - FIXED_AMOUNT override adds successfully")
    void testAddComponentValue_Fixed_Success() {
        EmployeeSalaryComponentValueRequest request = new EmployeeSalaryComponentValueRequest(
                1L, ComponentOverrideType.FIXED_AMOUNT, BigDecimal.valueOf(60000), null
        );

        when(employeeSalaryAssignmentRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(assignment));
        when(salaryComponentRepository.findByIdAndOrganizationId(1L, orgId)).thenReturn(Optional.of(basicComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(50L, 1L)).thenReturn(true);
        when(employeeSalaryComponentValueRepository.existsBySalaryAssignmentIdAndSalaryComponentId(10L, 1L)).thenReturn(false);
        when(employeeSalaryComponentValueRepository.save(any(EmployeeSalaryComponentValue.class))).thenAnswer(inv -> {
            EmployeeSalaryComponentValue val = inv.getArgument(0);
            val.setId(501L);
            return val;
        });

        EmployeeSalaryComponentValueResponse response = componentValueService.addComponentValue(100L, 10L, request);

        assertNotNull(response);
        assertEquals(501L, response.getId());
        assertEquals(ComponentOverrideType.FIXED_AMOUNT, response.getOverrideType());
        assertEquals(BigDecimal.valueOf(60000), response.getAmount());
    }

    @Test
    @DisplayName("Add Component Value - Reject if component not in assigned structure")
    void testAddComponentValue_NotInStructure_ThrowsBadRequest() {
        EmployeeSalaryComponentValueRequest request = new EmployeeSalaryComponentValueRequest(
                2L, ComponentOverrideType.PERCENTAGE, null, BigDecimal.valueOf(30)
        );

        when(employeeSalaryAssignmentRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(assignment));
        when(salaryComponentRepository.findByIdAndOrganizationId(2L, orgId)).thenReturn(Optional.of(hraComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(50L, 2L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> componentValueService.addComponentValue(100L, 10L, request));
    }

    @Test
    @DisplayName("Add Component Value - Reject duplicate value")
    void testAddComponentValue_Duplicate_ThrowsConflict() {
        EmployeeSalaryComponentValueRequest request = new EmployeeSalaryComponentValueRequest(
                1L, ComponentOverrideType.FIXED_AMOUNT, BigDecimal.valueOf(50000), null
        );

        when(employeeSalaryAssignmentRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(assignment));
        when(salaryComponentRepository.findByIdAndOrganizationId(1L, orgId)).thenReturn(Optional.of(basicComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(50L, 1L)).thenReturn(true);
        when(employeeSalaryComponentValueRepository.existsBySalaryAssignmentIdAndSalaryComponentId(10L, 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> componentValueService.addComponentValue(100L, 10L, request));
    }

    @Test
    @DisplayName("Get Component Values - Returns list of configured overrides")
    void testGetComponentValues() {
        EmployeeSalaryComponentValue val = new EmployeeSalaryComponentValue(
                assignment, basicComponent, BigDecimal.valueOf(50000), null, ComponentOverrideType.FIXED_AMOUNT
        );
        val.setId(501L);

        when(employeeSalaryAssignmentRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(assignment));
        when(employeeSalaryComponentValueRepository.findBySalaryAssignmentId(10L)).thenReturn(List.of(val));

        List<EmployeeSalaryComponentValueResponse> list = componentValueService.getComponentValues(100L, 10L);

        assertEquals(1, list.size());
        assertEquals("BASIC", list.get(0).getComponentCode());
        assertEquals(BigDecimal.valueOf(50000), list.get(0).getAmount());
    }

    @Test
    @DisplayName("Remove Component Value - Deletes override successfully")
    void testRemoveComponentValue_Success() {
        EmployeeSalaryComponentValue val = new EmployeeSalaryComponentValue(
                assignment, basicComponent, BigDecimal.valueOf(50000), null, ComponentOverrideType.FIXED_AMOUNT
        );
        val.setId(501L);

        when(employeeSalaryAssignmentRepository.findByIdAndOrganizationId(10L, orgId)).thenReturn(Optional.of(assignment));
        when(employeeSalaryComponentValueRepository.findByIdAndSalaryAssignmentId(501L, 10L)).thenReturn(Optional.of(val));

        componentValueService.removeComponentValue(100L, 10L, 501L);

        verify(employeeSalaryComponentValueRepository).delete(val);
    }
}
