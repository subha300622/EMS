package com.example.ems.payroll.service;

import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.payroll.dto.SalaryComponentCreateRequest;
import com.example.ems.payroll.dto.SalaryComponentResponse;
import com.example.ems.payroll.dto.SalaryComponentUpdateRequest;
import com.example.ems.payroll.entity.SalaryComponent;
import com.example.ems.payroll.entity.SalaryComponentType;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalaryComponentService {

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toUpperCase().replaceAll("[\\s-]+", "_");
    }

    @Transactional
    public SalaryComponentResponse createComponent(SalaryComponentCreateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        String normalizedCode = normalizeCode(request.getCode());

        if (salaryComponentRepository.existsByOrganizationIdAndCode(organizationId, normalizedCode)) {
            throw new ConflictException("Salary component with code '" + normalizedCode + "' already exists in this organization.");
        }

        SalaryComponent component = new SalaryComponent();
        component.setOrganizationId(organizationId);
        component.setName(request.getName().trim());
        component.setCode(normalizedCode);
        component.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        component.setComponentType(request.getComponentType());
        component.setTaxable(request.getTaxable() != null ? request.getTaxable() : true);
        component.setActive(request.getActive() != null ? request.getActive() : true);

        SalaryComponent saved = salaryComponentRepository.save(component);
        return SalaryComponentResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<SalaryComponentResponse> getComponents(SalaryComponentType type, Boolean active) {
        Long organizationId = TenantContext.requireOrganizationId();
        List<SalaryComponent> components;

        if (type != null && active != null) {
            components = salaryComponentRepository.findByOrganizationIdAndComponentTypeAndActive(organizationId, type, active);
        } else if (type != null) {
            components = salaryComponentRepository.findByOrganizationIdAndComponentType(organizationId, type);
        } else if (active != null) {
            components = salaryComponentRepository.findByOrganizationIdAndActive(organizationId, active);
        } else {
            components = salaryComponentRepository.findByOrganizationId(organizationId);
        }

        return components.stream()
                .map(SalaryComponentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SalaryComponentResponse getComponentById(Long componentId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryComponent component = salaryComponentRepository.findByIdAndOrganizationId(componentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary component not found with id: " + componentId));

        return SalaryComponentResponse.fromEntity(component);
    }

    @Transactional
    public SalaryComponentResponse updateComponent(Long componentId, SalaryComponentUpdateRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryComponent component = salaryComponentRepository.findByIdAndOrganizationId(componentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary component not found with id: " + componentId));

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            component.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            component.setDescription(request.getDescription().trim());
        }
        if (request.getTaxable() != null) {
            component.setTaxable(request.getTaxable());
        }
        if (request.getActive() != null) {
            component.setActive(request.getActive());
        }

        SalaryComponent updated = salaryComponentRepository.save(component);
        return SalaryComponentResponse.fromEntity(updated);
    }

    @Transactional
    public void deactivateComponent(Long componentId) {
        Long organizationId = TenantContext.requireOrganizationId();
        SalaryComponent component = salaryComponentRepository.findByIdAndOrganizationId(componentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Salary component not found with id: " + componentId));

        // Soft delete: deactivate component to preserve historical salary & payroll records
        component.setActive(false);
        salaryComponentRepository.save(component);
    }
}
