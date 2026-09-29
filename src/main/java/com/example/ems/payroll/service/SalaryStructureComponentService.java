package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.StructureComponentCreateRequest;
import com.example.ems.payroll.dto.StructureComponentResponse;
import com.example.ems.payroll.dto.StructureComponentUpdateRequest;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
import com.example.ems.payroll.repository.SalaryStructureRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaryStructureComponentService {

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    private SalaryStructure getStructureAndCheckMutability(Long structureId, Long organizationId) {
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        if (structure.getStatus() == SalaryStructureStatus.ACTIVE || structure.getStatus() == SalaryStructureStatus.INACTIVE) {
            throw new ConflictException("Cannot modify components of an " + structure.getStatus() +
                    " salary structure. Please create a new version instead.");
        }
        return structure;
    }

    private void validateCalculationRules(
            Long componentId,
            CalculationType calculationType,
            CalculationBaseType baseType,
            Long baseComponentId,
            BigDecimal fixedAmount,
            BigDecimal percentage,
            String formula,
            Long organizationId) {

        if (calculationType == null) {
            throw new BadRequestException("Calculation type is required.");
        }

        if (calculationType == CalculationType.FIXED) {
            if (percentage != null) {
                throw new BadRequestException("Percentage cannot be specified for FIXED calculation type.");
            }
            if (formula != null && !formula.trim().isEmpty()) {
                throw new BadRequestException("Formula cannot be specified for FIXED calculation type.");
            }
        } else if (calculationType == CalculationType.PERCENTAGE) {
            if (percentage == null || percentage.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BadRequestException("Percentage must be greater than 0 for PERCENTAGE calculation type.");
            }
            if (baseType == null || baseType == CalculationBaseType.NONE) {
                throw new BadRequestException("Calculation base type (COMPONENT or GROSS) is required for PERCENTAGE calculation type.");
            }
            if (baseType == CalculationBaseType.COMPONENT) {
                if (baseComponentId == null) {
                    throw new BadRequestException("Base component ID is required when calculation base type is COMPONENT.");
                }
                if (baseComponentId.equals(componentId)) {
                    throw new BadRequestException("A component cannot depend on itself as its calculation base.");
                }
                SalaryComponent baseComp = salaryComponentRepository.findByIdAndOrganizationId(baseComponentId, organizationId)
                        .orElseThrow(() -> new ResourceNotFoundException("Calculation base component not found with id: " + baseComponentId));
                if (!Boolean.TRUE.equals(baseComp.getActive())) {
                    throw new BadRequestException("Calculation base component '" + baseComp.getName() + "' is inactive.");
                }
            }
        } else if (calculationType == CalculationType.FORMULA) {
            if (formula == null || formula.trim().isEmpty()) {
                throw new BadRequestException("Formula expression is required for FORMULA calculation type.");
            }
        }
    }

    @Transactional
    public StructureComponentResponse addComponentToStructure(Long structureId, StructureComponentCreateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = getStructureAndCheckMutability(structureId, organizationId);

        SalaryComponent component = salaryComponentRepository.findByIdAndOrganizationId(request.getComponentId(), organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary component not found with id: " + request.getComponentId()));

        if (!Boolean.TRUE.equals(component.getActive())) {
            throw new BadRequestException("Cannot add inactive salary component '" + component.getName() + "' to a structure.");
        }

        if (salaryStructureComponentRepository.existsBySalaryStructureIdAndSalaryComponentId(structureId, request.getComponentId())) {
            throw new ConflictException("Component '" + component.getName() + "' is already attached to this salary structure.");
        }

        CalculationBaseType baseType = request.getCalculationBaseType() != null ? request.getCalculationBaseType() : CalculationBaseType.NONE;

        validateCalculationRules(
                component.getId(),
                request.getCalculationType(),
                baseType,
                request.getCalculationBaseComponentId(),
                request.getFixedAmount(),
                request.getPercentage(),
                request.getFormula(),
                organizationId
        );

        SalaryComponent baseComponent = null;
        if (request.getCalculationType() == CalculationType.PERCENTAGE && baseType == CalculationBaseType.COMPONENT && request.getCalculationBaseComponentId() != null) {
            baseComponent = salaryComponentRepository.findByIdAndOrganizationId(request.getCalculationBaseComponentId(), organizationId).orElse(null);
        }

        SalaryStructureComponent structureComponent = new SalaryStructureComponent();
        structureComponent.setSalaryStructure(structure);
        structureComponent.setSalaryComponent(component);
        structureComponent.setCalculationType(request.getCalculationType());
        structureComponent.setCalculationBaseType(baseType);
        structureComponent.setCalculationBaseComponent(baseComponent);
        structureComponent.setFixedAmount(request.getFixedAmount());
        structureComponent.setPercentage(request.getPercentage());
        structureComponent.setFormula(request.getFormula() != null ? request.getFormula().trim() : null);
        structureComponent.setCalculationOrder(request.getCalculationOrder() != null ? request.getCalculationOrder() : 1);

        SalaryStructureComponent saved = salaryStructureComponentRepository.save(structureComponent);

        // If structure was in VALIDATED status, reset to DRAFT as rules changed
        if (structure.getStatus() == SalaryStructureStatus.VALIDATED) {
            structure.setStatus(SalaryStructureStatus.DRAFT);
            salaryStructureRepository.save(structure);
        }

        return StructureComponentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<StructureComponentResponse> getStructureComponents(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        if (salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId).isEmpty()) {
            throw new ResourceNotFoundException("Salary structure not found with id: " + structureId);
        }

        List<SalaryStructureComponent> components = salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(structureId);
        return components.stream()
                .map(StructureComponentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public StructureComponentResponse updateStructureComponent(Long structureId, Long structureComponentId, StructureComponentUpdateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = getStructureAndCheckMutability(structureId, organizationId);

        SalaryStructureComponent structureComponent = salaryStructureComponentRepository.findByIdAndSalaryStructureId(structureComponentId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure component mapping not found with id: " + structureComponentId));

        CalculationType calculationType = request.getCalculationType() != null ? request.getCalculationType() : structureComponent.getCalculationType();
        CalculationBaseType baseType = request.getCalculationBaseType() != null ? request.getCalculationBaseType() : structureComponent.getCalculationBaseType();
        Long baseComponentId = request.getCalculationBaseComponentId() != null ? request.getCalculationBaseComponentId() :
                (structureComponent.getCalculationBaseComponent() != null ? structureComponent.getCalculationBaseComponent().getId() : null);

        validateCalculationRules(
                structureComponent.getSalaryComponent().getId(),
                calculationType,
                baseType,
                baseComponentId,
                request.getFixedAmount(),
                request.getPercentage(),
                request.getFormula(),
                organizationId
        );

        SalaryComponent baseComponent = null;
        if (calculationType == CalculationType.PERCENTAGE && baseType == CalculationBaseType.COMPONENT && baseComponentId != null) {
            baseComponent = salaryComponentRepository.findByIdAndOrganizationId(baseComponentId, organizationId).orElse(null);
        }

        structureComponent.setCalculationType(calculationType);
        structureComponent.setCalculationBaseType(baseType);
        structureComponent.setCalculationBaseComponent(baseComponent);

        if (request.getFixedAmount() != null) {
            structureComponent.setFixedAmount(request.getFixedAmount());
        }
        if (request.getPercentage() != null) {
            structureComponent.setPercentage(request.getPercentage());
        }
        if (request.getFormula() != null) {
            structureComponent.setFormula(request.getFormula().trim());
        }
        if (request.getCalculationOrder() != null) {
            structureComponent.setCalculationOrder(request.getCalculationOrder());
        }

        SalaryStructureComponent updated = salaryStructureComponentRepository.save(structureComponent);

        // Reset to DRAFT if VALIDATED
        if (structure.getStatus() == SalaryStructureStatus.VALIDATED) {
            structure.setStatus(SalaryStructureStatus.DRAFT);
            salaryStructureRepository.save(structure);
        }

        return StructureComponentResponse.fromEntity(updated);
    }

    @Transactional
    public void removeComponentFromStructure(Long structureId, Long structureComponentId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = getStructureAndCheckMutability(structureId, organizationId);

        SalaryStructureComponent structureComponent = salaryStructureComponentRepository.findByIdAndSalaryStructureId(structureComponentId, structureId)
                .orElseThrow(() -> new ResourceNotFoundException("Structure component mapping not found with id: " + structureComponentId));

        salaryStructureComponentRepository.delete(structureComponent);

        // Reset to DRAFT if VALIDATED
        if (structure.getStatus() == SalaryStructureStatus.VALIDATED) {
            structure.setStatus(SalaryStructureStatus.DRAFT);
            salaryStructureRepository.save(structure);
        }
    }
}
