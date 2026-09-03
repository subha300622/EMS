package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.calculation.SalaryCalculationEngine;
import com.example.ems.payroll.dto.EmployeeSalaryComponentValueRequest;
import com.example.ems.payroll.dto.SalaryCalculationPreviewRequest;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.EmployeeSalaryComponentValueRepository;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryCalculationService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    @Autowired
    private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Autowired
    private SalaryCalculationEngine salaryCalculationEngine;

    @Transactional(readOnly = true)
    public SalaryCalculationResponse calculateCurrentSalary(Long employeeId) {
        return calculateSalaryForDate(employeeId, LocalDate.now(), null);
    }

    @Transactional(readOnly = true)
    public SalaryCalculationResponse previewSalaryCalculation(Long employeeId, SalaryCalculationPreviewRequest request) {
        LocalDate targetDate = (request != null && request.getEffectiveDate() != null)
                ? request.getEffectiveDate()
                : LocalDate.now();
        List<EmployeeSalaryComponentValueRequest> adHocOverrides = (request != null) ? request.getOverrideValues() : null;

        return calculateSalaryForDate(employeeId, targetDate, adHocOverrides);
    }

    @Transactional(readOnly = true)
    public SalaryCalculationResponse calculateSalaryForDate(
            Long employeeId, LocalDate effectiveDate, List<EmployeeSalaryComponentValueRequest> adHocOverrides) {

        Long organizationId = TenantContext.requireOrganizationId();

        employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<EmployeeSalaryAssignment> assignments = employeeSalaryAssignmentRepository
                .findActiveAssignmentsForDate(organizationId, employeeId, effectiveDate);

        if (assignments.isEmpty()) {
            throw new ResourceNotFoundException("No active salary structure assignment found for employee id: " +
                    employeeId + " as of " + effectiveDate);
        }

        EmployeeSalaryAssignment assignment = assignments.get(0);
        SalaryStructure structure = assignment.getSalaryStructure();

        if (structure.getStatus() != SalaryStructureStatus.ACTIVE) {
            throw new BadRequestException("Assigned salary structure '" + structure.getName() + "' is in " +
                    structure.getStatus() + " status. Only ACTIVE structures can be calculated.");
        }

        List<SalaryStructureComponent> structureComponents = salaryStructureComponentRepository
                .findBySalaryStructureIdOrderByCalculationOrderAsc(structure.getId());

        if (structureComponents.isEmpty()) {
            throw new BadRequestException("Assigned salary structure has no components attached.");
        }

        Set<Long> structureCompIds = structureComponents.stream()
                .map(ssc -> ssc.getSalaryComponent().getId())
                .collect(Collectors.toSet());

        // 1. Load persisted employee overrides
        Map<Long, EmployeeSalaryComponentValue> effectiveOverrides = new HashMap<>();
        List<EmployeeSalaryComponentValue> persisted = employeeSalaryComponentValueRepository
                .findBySalaryAssignmentId(assignment.getId());
        for (EmployeeSalaryComponentValue val : persisted) {
            effectiveOverrides.put(val.getSalaryComponent().getId(), val);
        }

        // 2. Merge ad-hoc preview overrides (with structure membership verification)
        if (adHocOverrides != null) {
            for (EmployeeSalaryComponentValueRequest adHoc : adHocOverrides) {
                Long compId = adHoc.getSalaryComponentId();
                if (!structureCompIds.contains(compId)) {
                    throw new BadRequestException("Component ID " + compId + " is not part of the assigned salary structure.");
                }

                // Create synthetic in-memory override object
                SalaryStructureComponent matchSsc = structureComponents.stream()
                        .filter(ssc -> ssc.getSalaryComponent().getId().equals(compId))
                        .findFirst()
                        .orElse(null);

                if (matchSsc != null) {
                    EmployeeSalaryComponentValue syntheticOverride = new EmployeeSalaryComponentValue(
                            assignment,
                            matchSsc.getSalaryComponent(),
                            adHoc.getAmount(),
                            adHoc.getPercentage(),
                            adHoc.getOverrideType() != null ? adHoc.getOverrideType() : ComponentOverrideType.FIXED_AMOUNT
                    );
                    effectiveOverrides.put(compId, syntheticOverride);
                }
            }
        }

        // 3. Delegate to pure mathematical Calculation Engine
        return salaryCalculationEngine.calculate(assignment, structureComponents, effectiveOverrides, effectiveDate);
    }
}
