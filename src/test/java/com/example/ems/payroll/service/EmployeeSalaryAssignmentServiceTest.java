package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.EmployeeSalaryAssignmentCreateRequest;
import com.example.ems.payroll.dto.EmployeeSalaryAssignmentResponse;
import com.example.ems.payroll.dto.EmployeeSalaryComponentValueRequest;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.EmployeeSalaryComponentValueRepository;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeSalaryAssignmentServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryStructureRepository salaryStructureRepository;

    @Mock
    private SalaryComponentRepository salaryComponentRepository;

    @Mock
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Mock
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    @Mock
    private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

    @InjectMocks
    private EmployeeSalaryAssignmentService assignmentService;

    private final Long orgId = 1L;
    private Employee employee;
    private SalaryStructure activeStructure;
    private SalaryComponent basicComponent;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        employee = new Employee();
        employee.setId(100L);
        employee.setFullName("John Doe");
        employee.setEmployeeId("EMP-100");

        activeStructure = new SalaryStructure(orgId, "Engineering Standard", "ENG_STD", null, "INR", PayFrequency.MONTHLY, null, null);
        activeStructure.setId(50L);
        activeStructure.setStatus(SalaryStructureStatus.ACTIVE);
        activeStructure.setVersion(1);

        basicComponent = new SalaryComponent(orgId, "Basic Pay", "BASIC", null, SalaryComponentType.EARNING, true, true);
        basicComponent.setId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Create Assignment - Assigning ACTIVE structure succeeds and closes previous open assignment")
    void testCreateAssignment_Success_AutoClosesPrevious() {
        LocalDate newFrom = LocalDate.of(2026, 9, 1);

        EmployeeSalaryAssignment previousOpen = new EmployeeSalaryAssignment(
                orgId, employee, activeStructure, LocalDate.of(2026, 1, 1), null, SalaryAssignmentStatus.ACTIVE, "Initial"
        );
        previousOpen.setId(10L);

        EmployeeSalaryAssignmentCreateRequest request = new EmployeeSalaryAssignmentCreateRequest(
                50L, newFrom, null, "Annual Increment"
        );

        when(employeeRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(employee));
        when(salaryStructureRepository.findByIdAndOrganizationId(50L, orgId)).thenReturn(Optional.of(activeStructure));
        when(employeeSalaryAssignmentRepository.findClosedAssignmentsFrom(orgId, 100L, 0L, newFrom)).thenReturn(Collections.emptyList());
        when(employeeSalaryAssignmentRepository.findTopByOrganizationIdAndEmployeeIdAndEffectiveToIsNullAndStatusOrderByEffectiveFromDesc(orgId, 100L, SalaryAssignmentStatus.ACTIVE))
                .thenReturn(Optional.of(previousOpen));
        when(employeeSalaryAssignmentRepository.save(any(EmployeeSalaryAssignment.class))).thenAnswer(inv -> {
            EmployeeSalaryAssignment a = inv.getArgument(0);
            if (a.getId() == null) a.setId(11L);
            return a;
        });

        EmployeeSalaryAssignmentResponse response = assignmentService.createAssignment(100L, request);

        assertNotNull(response);
        assertEquals(11L, response.getId());
        assertEquals(SalaryAssignmentStatus.ACTIVE, response.getStatus());
        assertEquals(newFrom, response.getEffectiveFrom());
        assertNull(response.getEffectiveTo());

        // Verify previous open assignment was closed
        assertEquals(LocalDate.of(2026, 8, 31), previousOpen.getEffectiveTo());
        assertEquals(SalaryAssignmentStatus.ACTIVE, previousOpen.getStatus());
        verify(employeeSalaryAssignmentRepository).save(previousOpen);
    }

    @Test
    @DisplayName("Create Assignment - Reject if structure is not ACTIVE")
    void testCreateAssignment_NonActiveStructure_ThrowsBadRequest() {
        activeStructure.setStatus(SalaryStructureStatus.DRAFT);

        EmployeeSalaryAssignmentCreateRequest request = new EmployeeSalaryAssignmentCreateRequest(
                50L, LocalDate.of(2026, 9, 1), null, "Revision"
        );

        when(employeeRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(employee));
        when(salaryStructureRepository.findByIdAndOrganizationId(50L, orgId)).thenReturn(Optional.of(activeStructure));

        assertThrows(BadRequestException.class, () -> assignmentService.createAssignment(100L, request));
    }

    @Test
    @DisplayName("Create Assignment - Reject if dates overlap with closed historical assignment")
    void testCreateAssignment_OverlappingClosed_ThrowsConflict() {
        LocalDate newFrom = LocalDate.of(2026, 6, 1);
        LocalDate newTo = LocalDate.of(2026, 12, 31);

        EmployeeSalaryAssignment closedExisting = new EmployeeSalaryAssignment(
                orgId, employee, activeStructure, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 8, 31), SalaryAssignmentStatus.INACTIVE, "Old"
        );

        EmployeeSalaryAssignmentCreateRequest request = new EmployeeSalaryAssignmentCreateRequest(
                50L, newFrom, newTo, "Revision"
        );

        when(employeeRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(employee));
        when(salaryStructureRepository.findByIdAndOrganizationId(50L, orgId)).thenReturn(Optional.of(activeStructure));
        when(employeeSalaryAssignmentRepository.findClosedAssignmentsFrom(orgId, 100L, 0L, newFrom))
                .thenReturn(List.of(closedExisting));

        assertThrows(ConflictException.class, () -> assignmentService.createAssignment(100L, request));
    }

    @Test
    @DisplayName("Create Assignment - With initial component values saves overrides")
    void testCreateAssignment_WithComponentValues_Success() {
        EmployeeSalaryComponentValueRequest compValReq = new EmployeeSalaryComponentValueRequest(
                1L, ComponentOverrideType.FIXED_AMOUNT, BigDecimal.valueOf(50000), null
        );

        EmployeeSalaryAssignmentCreateRequest request = new EmployeeSalaryAssignmentCreateRequest(
                50L, LocalDate.of(2026, 9, 1), null, "Revision"
        );
        request.setComponentValues(List.of(compValReq));

        when(employeeRepository.findByIdAndOrganizationId(100L, orgId)).thenReturn(Optional.of(employee));
        when(salaryStructureRepository.findByIdAndOrganizationId(50L, orgId)).thenReturn(Optional.of(activeStructure));
        when(employeeSalaryAssignmentRepository.findClosedAssignmentsFrom(any(), any(), any(), any())).thenReturn(Collections.emptyList());
        when(employeeSalaryAssignmentRepository.findTopByOrganizationIdAndEmployeeIdAndEffectiveToIsNullAndStatusOrderByEffectiveFromDesc(any(), any(), any())).thenReturn(Optional.empty());
        when(salaryComponentRepository.findByIdAndOrganizationId(1L, orgId)).thenReturn(Optional.of(basicComponent));
        when(salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(50L, 1L)).thenReturn(true);
        when(employeeSalaryAssignmentRepository.save(any(EmployeeSalaryAssignment.class))).thenAnswer(inv -> {
            EmployeeSalaryAssignment a = inv.getArgument(0);
            a.setId(12L);
            return a;
        });

        EmployeeSalaryAssignmentResponse response = assignmentService.createAssignment(100L, request);

        assertNotNull(response);
        verify(employeeSalaryComponentValueRepository).save(any(EmployeeSalaryComponentValue.class));
    }
}
