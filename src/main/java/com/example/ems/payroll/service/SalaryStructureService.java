package com.example.ems.payroll.service;

import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.SalaryStructureCreateRequest;
import com.example.ems.payroll.dto.SalaryStructureResponse;
import com.example.ems.payroll.dto.SalaryStructureUpdateRequest;
import com.example.ems.payroll.entity.PayFrequency;
import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.entity.SalaryStructureStatus;
import com.example.ems.payroll.repository.SalaryStructureRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.example.ems.payroll.entity.SalaryStructureComponent;
import com.example.ems.payroll.repository.SalaryStructureComponentRepository;

@Service
public class SalaryStructureService {

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private SalaryStructureComponentRepository salaryStructureComponentRepository;

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toUpperCase().replaceAll("[\\s-]+", "_");
    }

    private void validateDates(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveFrom != null && effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new BadRequestException("effectiveTo date cannot be earlier than effectiveFrom date.");
        }
    }

    @Transactional
    public SalaryStructureResponse createStructure(SalaryStructureCreateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        String normalizedCode = normalizeCode(request.getCode());

        if (salaryStructureRepository.existsByOrganizationIdAndCodeAndVersion(organizationId, normalizedCode, 1)) {
            throw new ConflictException("Salary structure with code '" + normalizedCode + "' version 1 already exists in this organization.");
        }

        validateDates(request.getEffectiveFrom(), request.getEffectiveTo());

        SalaryStructure structure = new SalaryStructure();
        structure.setOrganizationId(organizationId);
        structure.setName(request.getName().trim());
        structure.setCode(normalizedCode);
        structure.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        structure.setCurrency(request.getCurrency() != null && !request.getCurrency().trim().isEmpty() ? request.getCurrency().trim().toUpperCase() : "INR");
        structure.setPayFrequency(request.getPayFrequency() != null ? request.getPayFrequency() : PayFrequency.MONTHLY);
        structure.setEffectiveFrom(request.getEffectiveFrom());
        structure.setEffectiveTo(request.getEffectiveTo());
        structure.setStatus(SalaryStructureStatus.DRAFT);
        structure.setVersion(1);

        SalaryStructure saved = salaryStructureRepository.save(structure);
        return SalaryStructureResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SalaryStructureResponse> getStructures(SalaryStructureStatus status, String search) {
        Long organizationId = TenantContext.requireOrganizationId();
        String trimmedSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        List<SalaryStructure> structures = salaryStructureRepository.searchStructures(organizationId, status, trimmedSearch);
        return structures.stream()
                .map(SalaryStructureResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalaryStructureResponse getStructureById(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        return SalaryStructureResponse.fromEntity(structure);
    }

    @Transactional
    public SalaryStructureResponse updateStructure(Long structureId, SalaryStructureUpdateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        if (structure.getStatus() == SalaryStructureStatus.ACTIVE || structure.getStatus() == SalaryStructureStatus.INACTIVE) {
            throw new ConflictException("Active or Inactive salary structures cannot be modified directly. Please branch a new version instead.");
        }

        LocalDate newEffectiveFrom = request.getEffectiveFrom() != null ? request.getEffectiveFrom() : structure.getEffectiveFrom();
        LocalDate newEffectiveTo = request.getEffectiveTo() != null ? request.getEffectiveTo() : structure.getEffectiveTo();
        validateDates(newEffectiveFrom, newEffectiveTo);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            structure.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            structure.setDescription(request.getDescription().trim());
        }
        if (request.getCurrency() != null && !request.getCurrency().trim().isEmpty()) {
            structure.setCurrency(request.getCurrency().trim().toUpperCase());
        }
        if (request.getPayFrequency() != null) {
            structure.setPayFrequency(request.getPayFrequency());
        }
        if (request.getEffectiveFrom() != null) {
            structure.setEffectiveFrom(request.getEffectiveFrom());
        }
        if (request.getEffectiveTo() != null) {
            structure.setEffectiveTo(request.getEffectiveTo());
        }

        // Reset status to DRAFT if it was VALIDATED, as metadata changed
        if (structure.getStatus() == SalaryStructureStatus.VALIDATED) {
            structure.setStatus(SalaryStructureStatus.DRAFT);
        }

        SalaryStructure updated = salaryStructureRepository.save(structure);
        return SalaryStructureResponse.fromEntity(updated);
    }

    @Transactional
    public SalaryStructureResponse createNewVersion(Long structureId, SalaryStructureCreateRequest overrideRequest) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure current = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        SalaryStructure topVersion = salaryStructureRepository.findTopByOrganizationIdAndCodeOrderByVersionDesc(organizationId, current.getCode())
                .orElse(current);

        int newVersion = topVersion.getVersion() + 1;

        SalaryStructure next = new SalaryStructure();
        next.setOrganizationId(organizationId);
        next.setCode(current.getCode());
        next.setName(overrideRequest != null && overrideRequest.getName() != null ? overrideRequest.getName().trim() : current.getName());
        next.setDescription(overrideRequest != null && overrideRequest.getDescription() != null ? overrideRequest.getDescription().trim() : current.getDescription());
        next.setCurrency(overrideRequest != null && overrideRequest.getCurrency() != null ? overrideRequest.getCurrency().trim().toUpperCase() : current.getCurrency());
        next.setPayFrequency(overrideRequest != null && overrideRequest.getPayFrequency() != null ? overrideRequest.getPayFrequency() : current.getPayFrequency());
        next.setEffectiveFrom(overrideRequest != null && overrideRequest.getEffectiveFrom() != null ? overrideRequest.getEffectiveFrom() : null);
        next.setEffectiveTo(overrideRequest != null && overrideRequest.getEffectiveTo() != null ? overrideRequest.getEffectiveTo() : null);
        next.setStatus(SalaryStructureStatus.DRAFT);
        next.setVersion(newVersion);

        validateDates(next.getEffectiveFrom(), next.getEffectiveTo());

        SalaryStructure saved = salaryStructureRepository.save(next);

        // Deep-copy existing structure components into the new draft version
        List<SalaryStructureComponent> existingComponents = salaryStructureComponentRepository.findBySalaryStructureIdOrderByCalculationOrderAsc(structureId);
        for (SalaryStructureComponent existing : existingComponents) {
            SalaryStructureComponent copy = new SalaryStructureComponent();
            copy.setSalaryStructure(saved);
            copy.setSalaryComponent(existing.getSalaryComponent());
            copy.setCalculationType(existing.getCalculationType());
            copy.setCalculationBaseType(existing.getCalculationBaseType());
            copy.setCalculationBaseComponent(existing.getCalculationBaseComponent());
            copy.setFixedAmount(existing.getFixedAmount());
            copy.setPercentage(existing.getPercentage());
            copy.setFormula(existing.getFormula());
            copy.setCalculationOrder(existing.getCalculationOrder());
            salaryStructureComponentRepository.save(copy);
        }

        return SalaryStructureResponse.fromEntity(saved);
    }

    @Transactional
    public SalaryStructureResponse validateStructure(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        if (structure.getStatus() == SalaryStructureStatus.INACTIVE) {
            throw new BadRequestException("Cannot validate an INACTIVE salary structure.");
        }

        if (structure.getName() == null || structure.getName().trim().isEmpty()) {
            throw new BadRequestException("Salary structure name cannot be blank.");
        }
        if (structure.getCode() == null || structure.getCode().trim().isEmpty()) {
            throw new BadRequestException("Salary structure code cannot be blank.");
        }
        if (structure.getCurrency() == null || structure.getCurrency().trim().isEmpty()) {
            throw new BadRequestException("Salary structure currency cannot be blank.");
        }
        if (structure.getPayFrequency() == null) {
            throw new BadRequestException("Salary structure pay frequency cannot be null.");
        }

        validateDates(structure.getEffectiveFrom(), structure.getEffectiveTo());

        structure.setStatus(SalaryStructureStatus.VALIDATED);
        SalaryStructure saved = salaryStructureRepository.save(structure);
        return SalaryStructureResponse.fromEntity(saved);
    }

    @Transactional
    public SalaryStructureResponse activateStructure(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        if (structure.getStatus() == SalaryStructureStatus.INACTIVE) {
            throw new BadRequestException("Cannot activate an INACTIVE salary structure.");
        }

        validateDates(structure.getEffectiveFrom(), structure.getEffectiveTo());

        structure.setStatus(SalaryStructureStatus.ACTIVE);
        SalaryStructure saved = salaryStructureRepository.save(structure);
        return SalaryStructureResponse.fromEntity(saved);
    }

    @Transactional
    public SalaryStructureResponse deactivateStructure(Long structureId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryStructure structure = salaryStructureRepository.findByIdAndOrganizationId(structureId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary structure not found with id: " + structureId));

        structure.setStatus(SalaryStructureStatus.INACTIVE);
        SalaryStructure saved = salaryStructureRepository.save(structure);
        return SalaryStructureResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteStructure(Long structureId) {
        deactivateStructure(structureId);
    }
}
