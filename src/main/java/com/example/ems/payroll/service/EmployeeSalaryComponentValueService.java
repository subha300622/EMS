package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.EmployeeSalaryComponentValueRequest;
import com.example.ems.payroll.dto.EmployeeSalaryComponentValueResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.EmployeeSalaryAssignmentRepository;
import com.example.ems.payroll.repository.EmployeeSalaryComponentValueRepository;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeSalaryComponentValueService {

    @Autowired
    private EmployeeSalaryAssignmentRepository employeeSalaryAssignmentRepository;

    @Autowired
    private EmployeeSalaryComponentValueRepository employeeSalaryComponentValueRepository;

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    private EmployeeSalaryAssignment getAssignmentAndVerify(Long employeeId, Long assignmentId, Long organizationId) {
        EmployeeSalaryAssignment assignment = employeeSalaryAssignmentRepository.findByIdAndOrganizationId(assignmentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary assignment not found with id: " + assignmentId));

        if (!assignment.getEmployee().getId().equals(employeeId)) {
            throw new BadRequestException("Salary assignment id " + assignmentId + " does not belong to employee id " + employeeId);
        }
        return assignment;
    }

    private void validateOverride(ComponentOverrideType overrideType, BigDecimal amount, BigDecimal percentage) {
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
    public EmployeeSalaryComponentValueResponse addComponentValue(
            Long employeeId, Long assignmentId, EmployeeSalaryComponentValueRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        EmployeeSalaryAssignment assignment = getAssignmentAndVerify(employeeId, assignmentId, organizationId);

        SalaryComponent component = salaryComponentRepository.findByIdAndOrganizationId(request.getSalaryComponentId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary component not found with id: " + request.getSalaryComponentId()));

        if (!Boolean.TRUE.equals(component.getActive())) {
            throw new BadRequestException("Cannot assign value for inactive salary component '" + component.getName() + "'.");
        }

        boolean inStructure = salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(
                assignment.getSalaryStructure().getId(), component.getId());
        if (!inStructure) {
            throw new BadRequestException("Component '" + component.getName() + "' (" + component.getCode() +
                    ") is not part of the assigned salary structure.");
        }

        if (employeeSalaryComponentValueRepository.existsBySalaryAssignmentIdAndSalaryComponentId(assignmentId, component.getId())) {
            throw new ConflictException("Component value already exists for '" + component.getName() +
                    "' on this assignment. Use PUT to update instead.");
        }

        validateOverride(request.getOverrideType(), request.getAmount(), request.getPercentage());

        BigDecimal amt = request.getOverrideType() == ComponentOverrideType.FIXED_AMOUNT ? request.getAmount() : null;
        BigDecimal pct = request.getOverrideType() == ComponentOverrideType.PERCENTAGE ? request.getPercentage() : null;

        EmployeeSalaryComponentValue value = new EmployeeSalaryComponentValue(
                assignment, component, amt, pct, request.getOverrideType()
        );
        EmployeeSalaryComponentValue saved = employeeSalaryComponentValueRepository.save(value);

        return EmployeeSalaryComponentValueResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EmployeeSalaryComponentValueResponse> getComponentValues(Long employeeId, Long assignmentId) {
        Long organizationId = TenantContext.requireOrganizationId();
        getAssignmentAndVerify(employeeId, assignmentId, organizationId);

        List<EmployeeSalaryComponentValue> values = employeeSalaryComponentValueRepository.findBySalaryAssignmentId(assignmentId);
        return values.stream()
                .map(EmployeeSalaryComponentValueResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmployeeSalaryComponentValueResponse updateComponentValue(
            Long employeeId, Long assignmentId, Long valueId, EmployeeSalaryComponentValueRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        getAssignmentAndVerify(employeeId, assignmentId, organizationId);

        EmployeeSalaryComponentValue value = employeeSalaryComponentValueRepository.findByIdAndSalaryAssignmentId(valueId, assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Component value not found with id: " + valueId));

        validateOverride(request.getOverrideType(), request.getAmount(), request.getPercentage());

        BigDecimal amt = request.getOverrideType() == ComponentOverrideType.FIXED_AMOUNT ? request.getAmount() : null;
        BigDecimal pct = request.getOverrideType() == ComponentOverrideType.PERCENTAGE ? request.getPercentage() : null;

        value.setOverrideType(request.getOverrideType());
        value.setAmount(amt);
        value.setPercentage(pct);

        EmployeeSalaryComponentValue updated = employeeSalaryComponentValueRepository.save(value);
        return EmployeeSalaryComponentValueResponse.fromEntity(updated);
    }

    @Transactional
    public void removeComponentValue(Long employeeId, Long assignmentId, Long valueId) {
        Long organizationId = TenantContext.requireOrganizationId();
        getAssignmentAndVerify(employeeId, assignmentId, organizationId);

        EmployeeSalaryComponentValue value = employeeSalaryComponentValueRepository.findByIdAndSalaryAssignmentId(valueId, assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Component value not found with id: " + valueId));

        employeeSalaryComponentValueRepository.delete(value);
    }
}
