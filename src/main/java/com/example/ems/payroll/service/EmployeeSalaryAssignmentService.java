package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeSalaryAssignmentService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Autowired
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    @Autowired
    private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

    private void validateOverrideValue(ComponentOverrideType overrideType, BigDecimal amount, BigDecimal percentage) {
        if (overrideType == null) {
            throw new BadRequestException("Override type is required.");
        }
        if (overrideType == ComponentOverrideType.FIXED_AMOUNT) {
            if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("A valid non-negative amount is required for FIXED_AMOUNT override.");
            }
        } else if (overrideType == ComponentOverrideType.PERCENTAGE) {
            if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("A valid percentage greater than 0 is required for PERCENTAGE override.");
            }
        }
    }

    @Transactional
    public EmployeeSalaryAssignmentResponse createAssignment(Long employeeId, EmployeeSalaryAssignmentCreateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();

        Employee employee = employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(request.getSalaryStructureId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + request.getSalaryStructureId()));

        if (structure.getStatus() != SalaryStructureStatus.ACTIVE) {
            throw new BadRequestException("Cannot assign salary structure '" + structure.getName() +
                    "' because it is in " + structure.getStatus() + " status. Only ACTIVE structures can be assigned.");
        }

        LocalDate effectiveFrom = request.getEffectiveFrom();
        LocalDate effectiveTo = request.getEffectiveTo();

        if (effectiveFrom == null) {
            throw new BadRequestException("Effective from date is required.");
        }
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new BadRequestException("effectiveTo date (" + effectiveTo + ") cannot be earlier than effectiveFrom date (" + effectiveFrom + ").");
        }

        // 1. Check for overlapping historical closed assignments
        List<EmployeeSalaryAssignment> candidates = employeeSalaryAssignmentRepository.findClosedAssignmentsFrom(
                organizationId, employeeId, 0L, effectiveFrom);
        List<EmployeeSalaryAssignment> overlaps = candidates.stream()
                .filter(a -> effectiveTo == null || !a.getEffectiveFrom().isAfter(effectiveTo))
                .toList();
        if (!overlaps.isEmpty()) {
            EmployeeSalaryAssignment conflicting = overlaps.get(0);
            throw new ConflictException("Salary assignment dates (" + effectiveFrom + " to " +
                    (effectiveTo != null ? effectiveTo : "open") + ") overlap with an existing assignment (" +
                    conflicting.getEffectiveFrom() + " to " + conflicting.getEffectiveTo() + ").");
        }

        // 2. Handle previous open-ended active assignment
        Optional<EmployeeSalaryAssignment> openAssignmentOpt = employeeSalaryAssignmentRepository
                .findTopByOrganizationIdAndEmployeeIdAndEffectiveToIsNullAndStatusOrderByEffectiveFromDesc(
                        organizationId, employeeId, SalaryAssignmentStatus.ACTIVE);

        if (openAssignmentOpt.isPresent()) {
            EmployeeSalaryAssignment openAssignment = openAssignmentOpt.get();
            if (!effectiveFrom.isAfter(openAssignment.getEffectiveFrom())) {
                throw new ConflictException("New assignment effectiveFrom (" + effectiveFrom +
                        ") must be after current active assignment effectiveFrom (" + openAssignment.getEffectiveFrom() + ").");
            }
            // Auto-close previous assignment smoothly
            openAssignment.setEffectiveTo(effectiveFrom.minusDays(1));
            employeeSalaryAssignmentRepository.save(openAssignment);
        }

        // 3. Create new assignment
        EmployeeSalaryAssignment assignment = new EmployeeSalaryAssignment(
                organizationId, employee, structure, effectiveFrom, effectiveTo, SalaryAssignmentStatus.ACTIVE, request.getReason()
        );
        EmployeeSalaryAssignment savedAssignment = employeeSalaryAssignmentRepository.save(assignment);

        // 4. Save initial employee component values if provided
        if (request.getComponentValues() != null && !request.getComponentValues().isEmpty()) {
            for (EmployeeSalaryComponentValueRequest valReq : request.getComponentValues()) {
                SalaryComponent comp = salaryComponentRepository.findByIdAndOrganizationId(valReq.getSalaryComponentId(), organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Salary component not found with id: " + valReq.getSalaryComponentId()));

                if (!Boolean.TRUE.equals(comp.getActive())) {
                    throw new BadRequestException("Cannot assign value for inactive salary component '" + comp.getName() + "'.");
                }

                boolean inStructure = salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(structure.getId(), comp.getId());
                if (!inStructure) {
                    throw new BadRequestException("Component '" + comp.getName() + "' (" + comp.getCode() + ") is not part of the assigned salary structure.");
                }

                validateOverrideValue(valReq.getOverrideType(), valReq.getAmount(), valReq.getPercentage());

                BigDecimal amt = valReq.getOverrideType() == ComponentOverrideType.FIXED_AMOUNT ? valReq.getAmount() : null;
                BigDecimal pct = valReq.getOverrideType() == ComponentOverrideType.PERCENTAGE ? valReq.getPercentage() : null;

                EmployeeSalaryComponentValue compValue = new EmployeeSalaryComponentValue(
                        savedAssignment, comp, amt, pct, valReq.getOverrideType()
                );
                employeeSalaryComponentValueRepository.save(compValue);
                savedAssignment.getComponentValues().add(compValue);
            }
        }

        return EmployeeSalaryAssignmentResponse.fromEntity(savedAssignment);
    }

    @Transactional(readOnly = true)
    public EmployeeSalaryAssignmentResponse getCurrentAssignment(Long employeeId) {
        Long organizationId = TenantContext.requireOrganizationId();
        employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        LocalDate today = LocalDate.now();
        List<EmployeeSalaryAssignment> activeAssignments = employeeSalaryAssignmentRepository.findActiveAssignmentsForDate(
                organizationId, employeeId, today);

        if (activeAssignments.isEmpty()) {
            throw new ResourceNotFoundException("No active salary assignment found for employee id: " + employeeId + " as of " + today);
        }

        return EmployeeSalaryAssignmentResponse.fromEntity(activeAssignments.get(0));
    }

    @Transactional(readOnly = true)
    public List<EmployeeSalaryAssignmentResponse> getAssignmentHistory(Long employeeId) {
        Long organizationId = TenantContext.requireOrganizationId();
        employeeRepository.findByIdAndOrganizationId(employeeId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        List<EmployeeSalaryAssignment> assignments = employeeSalaryAssignmentRepository
                .findByOrganizationIdAndEmployeeIdOrderByEffectiveFromDesc(organizationId, employeeId);

        return assignments.stream()
                .map(EmployeeSalaryAssignmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmployeeSalaryAssignmentResponse getAssignmentById(Long employeeId, Long assignmentId) {
        Long organizationId = TenantContext.requireOrganizationId();
        EmployeeSalaryAssignment assignment = employeeSalaryAssignmentRepository.findByIdAndOrganizationId(assignmentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary assignment not found with id: " + assignmentId));

        if (!assignment.getEmployee().getId().equals(employeeId)) {
            throw new BadRequestException("Salary assignment id " + assignmentId + " does not belong to employee id " + employeeId);
        }

        return EmployeeSalaryAssignmentResponse.fromEntity(assignment);
    }
}
