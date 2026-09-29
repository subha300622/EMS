package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.SalaryDependencyGraphResponse;
import com.example.ems.payroll.entity.*;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;
import com.example.ems.payroll.repository.SalaryStructureRepository;
import com.example.ems.payroll.validation.*;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryStructureValidationService {

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    @Autowired
    private SalaryDependencyGraphService salaryDependencyGraphService;

    private void validateDates(LocalDate effectiveFrom, LocalDate effectiveTo, List<SalaryValidationError> errors) {
        if (effectiveFrom != null && effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            errors.add(SalaryValidationError.of(SalaryValidationErrorType.INVALID_DATES,
                    "effectiveTo date (" + effectiveTo + ") cannot be earlier than effectiveFrom date (" + effectiveFrom + ")."));
        }
    }

    @Transactional
    public SalaryValidationResult validateStructure(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        SalaryValidationResult result = new SalaryValidationResult(false, structure.getId(), structure.getCode(), structure.getStatus());

        if (structure.getStatus() == SalaryStructureStatus.INACTIVE) {
            result.getErrors().add(SalaryValidationError.of(SalaryValidationErrorType.STRUCTURE_INACTIVE, "Cannot validate an INACTIVE salary structure."));
            return result;
        }

        validateDates(structure.getEffectiveFrom(), structure.getEffectiveTo(), result.getErrors());

        List<SalaryStructureComponent> components = salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(structureId);

        if (components.isEmpty()) {
            result.getErrors().add(SalaryValidationError.of(SalaryValidationErrorType.STRUCTURE_EMPTY,
                    "Salary structure has no components attached. Add at least one component before validation."));
            return result;
        }

        Set<Long> attachedComponentIds = components.stream()
                .map(ssc -> ssc.getSalaryComponent().getId())
                .collect(Collectors.toSet());

        // 1. Component integrity & base dependency checks
        for (SalaryStructureComponent ssc : components) {
            SalaryComponent comp = ssc.getSalaryComponent();

            if (!Boolean.TRUE.equals(comp.getActive())) {
                result.getErrors().add(SalaryValidationError.ofComponent(comp.getCode(), comp.getId(),
                        SalaryValidationErrorType.COMPONENT_INACTIVE,
                        "Salary component '" + comp.getName() + "' (" + comp.getCode() + ") is inactive."));
            }

            if (ssc.getCalculationType() == CalculationType.PERCENTAGE
                    && ssc.getCalculationBaseType() == CalculationBaseType.COMPONENT) {

                if (ssc.getCalculationBaseComponent() == null) {
                    result.getErrors().add(SalaryValidationError.ofComponent(comp.getCode(), comp.getId(),
                            SalaryValidationErrorType.INVALID_RULE,
                            "Component '" + comp.getName() + "' is configured as PERCENTAGE of COMPONENT but has no base component specified."));
                } else {
                    SalaryComponent baseComp = ssc.getCalculationBaseComponent();

                    if (baseComp.getId().equals(comp.getId())) {
                        result.getErrors().add(SalaryValidationError.ofComponent(comp.getCode(), comp.getId(),
                                SalaryValidationErrorType.SELF_DEPENDENCY,
                                "Component '" + comp.getName() + "' cannot depend on itself as its calculation base."));
                    } else if (!attachedComponentIds.contains(baseComp.getId())) {
                        result.getErrors().add(SalaryValidationError.ofComponent(comp.getCode(), comp.getId(),
                                SalaryValidationErrorType.DEPENDENCY_NOT_FOUND,
                                "Component '" + comp.getName() + "' depends on '" + baseComp.getName() + "' (" + baseComp.getCode() +
                                        "), but '" + baseComp.getName() + "' is not included in this salary structure."));
                    }
                }
            }
        }

        // If basic errors exist, return early without DAG cycle analysis
        if (!result.getErrors().isEmpty()) {
            structure.setStatus(SalaryStructureStatus.DRAFT);
            salaryStructureRepository.save(structure);
            result.setStatus(SalaryStructureStatus.DRAFT);
            return result;
        }

        // 2. Build DAG and run Cycle Detection
        Map<Long, SalaryDependencyNode> graph = salaryDependencyGraphService.buildGraph(components);
        Optional<String> cycleOpt = salaryDependencyGraphService.detectCycle(graph);

        if (cycleOpt.isPresent()) {
            String cyclePath = cycleOpt.get();
            result.getErrors().add(SalaryValidationError.of(SalaryValidationErrorType.CIRCULAR_DEPENDENCY,
                    "Circular dependency detected: " + cyclePath));
            structure.setStatus(SalaryStructureStatus.DRAFT);
            salaryStructureRepository.save(structure);
            result.setStatus(SalaryStructureStatus.DRAFT);
            return result;
        }

        // 3. Topological sort to determine calculation order
        List<Long> sortedComponentIds = salaryDependencyGraphService.topologicalSort(graph);

        if (sortedComponentIds.size() != components.size()) {
            result.getErrors().add(SalaryValidationError.of(SalaryValidationErrorType.CIRCULAR_DEPENDENCY,
                    "Unresolvable dependency graph detected across components."));
            structure.setStatus(SalaryStructureStatus.DRAFT);
            salaryStructureRepository.save(structure);
            result.setStatus(SalaryStructureStatus.DRAFT);
            return result;
        }

        // 4. Auto-update calculation_order in database to match topological sort order
        Map<Long, SalaryStructureComponent> compMap = components.stream()
                .collect(Collectors.toMap(ssc -> ssc.getSalaryComponent().getId(), ssc -> ssc));

        List<String> orderedCodes = new ArrayList<>();
        for (int i = 0; i < sortedComponentIds.size(); i++) {
            Long compId = sortedComponentIds.get(i);
            SalaryStructureComponent ssc = compMap.get(compId);
            if (ssc != null) {
                int newOrder = i + 1;
                if (ssc.getCalculationOrder() != newOrder) {
                    ssc.setCalculationOrder(newOrder);
                    salaryStructureComponentRepository.save(ssc);
                }
                orderedCodes.add(ssc.getSalaryComponent().getCode());
            }
        }

        // 5. Advance status to VALIDATED
        structure.setStatus(SalaryStructureStatus.VALIDATED);
        salaryStructureRepository.save(structure);

        result.setValid(true);
        result.setStatus(SalaryStructureStatus.VALIDATED);
        result.setCalculationOrder(orderedCodes);
        return result;
    }

    @Transactional
    public SalaryValidationResult activateStructure(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        if (structure.getStatus() == SalaryStructureStatus.INACTIVE) {
            throw new BadRequestException("Cannot activate an INACTIVE salary structure.");
        }

        // Re-run full validation
        SalaryValidationResult valResult = validateStructure(structureId);

        if (!valResult.isValid()) {
            String firstError = valResult.getErrors().isEmpty() ? "Validation failed" : valResult.getErrors().get(0).getMessage();
            throw new BadRequestException("Cannot activate salary structure due to validation errors: " + firstError);
        }

        // Transition to ACTIVE
        structure.setStatus(SalaryStructureStatus.ACTIVE);
        salaryStructureRepository.save(structure);

        valResult.setStatus(SalaryStructureStatus.ACTIVE);
        return valResult;
    }

    @Transactional(readOnly = true)
    public SalaryDependencyGraphResponse getDependencyGraph(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        SalaryDependencyGraphResponse response = new SalaryDependencyGraphResponse(structure.getId(), structure.getCode());

        List<SalaryStructureComponent> components = salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(structureId);
        Map<Long, SalaryDependencyNode> graph = salaryDependencyGraphService.buildGraph(components);

        Optional<String> cycleOpt = salaryDependencyGraphService.detectCycle(graph);
        response.setHasCycle(cycleOpt.isPresent());
        cycleOpt.ifPresent(response::setCyclePath);

        if (!cycleOpt.isPresent() && !components.isEmpty()) {
            List<Long> sortedIds = salaryDependencyGraphService.topologicalSort(graph);
            Map<Long, String> codeMap = components.stream()
                    .collect(Collectors.toMap(ssc -> ssc.getSalaryComponent().getId(), ssc -> ssc.getSalaryComponent().getCode()));

            List<String> order = sortedIds.stream()
                    .map(codeMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            response.setCalculationOrder(order);
        }

        for (SalaryStructureComponent ssc : components) {
            SalaryComponent comp = ssc.getSalaryComponent();
            List<String> dependsOn = new ArrayList<>();
            if (ssc.getCalculationType() == CalculationType.PERCENTAGE
                    && ssc.getCalculationBaseType() == CalculationBaseType.COMPONENT
                    && ssc.getCalculationBaseComponent() != null) {
                dependsOn.add(ssc.getCalculationBaseComponent().getCode());
            } else if (ssc.getCalculationType() == CalculationType.PERCENTAGE
                    && ssc.getCalculationBaseType() == CalculationBaseType.GROSS) {
                dependsOn.add("GROSS");
            }

            response.getComponents().add(new SalaryDependencyGraphResponse.ComponentDependencyItem(
                    comp.getId(),
                    comp.getCode(),
                    comp.getName(),
                    ssc.getCalculationType().name(),
                    dependsOn
            ));
        }

        return response;
    }
}
