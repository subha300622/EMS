package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.AppraisalCycleResponseDto;
import com.example.ems.appraisal.dto.CreateAppraisalCycleDto;
import com.example.ems.appraisal.dto.CycleEligibilityCriteriaDto;
import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalCycle;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.security.context.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppraisalCycleService {

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private com.example.ems.appraisal.repository.AppraisalConfigurationRepository configRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AppraisalCycleResponseDto createCycle(CreateAppraisalCycleDto dto, Employee creator) {
        Long orgId = TenantContext.requireOrganizationId();
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalStateException("Organization not found: " + orgId));

        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Cycle endDate cannot be before startDate");
        }

        if (cycleRepository.findByOrganizationId(orgId).stream().anyMatch(c -> c.getName().equalsIgnoreCase(dto.getName().trim()))) {
            throw new com.example.ems.common.exception.ConflictException("An appraisal cycle with name '" + dto.getName().trim() + "' already exists.");
        }

        AppraisalCycle cycle = new AppraisalCycle();
        cycle.setOrganization(org);
        cycle.setName(dto.getName().trim());
        cycle.setType(dto.getType() != null ? dto.getType().toUpperCase() : "ANNUAL");
        cycle.setStartDate(dto.getStartDate());
        cycle.setEndDate(dto.getEndDate());
        cycle.setStatus("DRAFT");
        cycle.setCreatedBy(creator);
        cycle.setCreatedAt(LocalDateTime.now());
        cycle.setUpdatedAt(LocalDateTime.now());

        if (dto.getEligibleEmployeeCriteria() != null) {
            try {
                cycle.setEligibleCriteriaJson(objectMapper.writeValueAsString(dto.getEligibleEmployeeCriteria()));
            } catch (Exception ignored) {}
        }

        return mapToDto(cycleRepository.save(cycle));
    }

    public List<AppraisalCycleResponseDto> getCycles() {
        Long orgId = TenantContext.requireOrganizationId();
        return cycleRepository.findByOrganizationId(orgId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AppraisalCycleResponseDto getCycleById(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));
        return mapToDto(cycle);
    }

    @Transactional
    public AppraisalCycleResponseDto activateCycle(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        cycle.setStatus("OPEN");
        cycle.setUpdatedAt(LocalDateTime.now());
        return mapToDto(cycleRepository.save(cycle));
    }

    @Transactional
    public AppraisalCycleResponseDto closeCycle(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        cycle.setStatus("CLOSED");
        cycle.setUpdatedAt(LocalDateTime.now());
        return mapToDto(cycleRepository.save(cycle));
    }

    /**
     * Flow B: Regular HR Appraisal Cycle batch generator.
     * Evaluates organization employee eligibility server-side and creates Appraisal records directly.
     */
    @Transactional
    public Map<String, Object> generateAppraisalsForCycle(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        if (!"OPEN".equalsIgnoreCase(cycle.getStatus()) && !"IN_PROGRESS".equalsIgnoreCase(cycle.getStatus())) {
            throw new IllegalStateException("Cannot generate appraisals for cycle with status: " + cycle.getStatus() + ". Cycle must be activated (OPEN) first.");
        }

        // Check organization initiation mode
        configRepository.findByOrganizationId(orgId).ifPresent(config -> {
            if (config.getInitiationMode() == com.example.ems.appraisal.entity.AppraisalInitiationMode.EMPLOYEE_ONLY) {
                throw new IllegalStateException("Appraisal initiation mode is configured as EMPLOYEE_ONLY. HR batch generation is disabled for this organization.");
            }
        });

        CycleEligibilityCriteriaDto criteria = null;
        if (cycle.getEligibleCriteriaJson() != null) {
            try {
                criteria = objectMapper.readValue(cycle.getEligibleCriteriaJson(), CycleEligibilityCriteriaDto.class);
            } catch (Exception ignored) {}
        }

        int minMonths = (criteria != null && criteria.getMinimumServiceMonths() != null)
                ? criteria.getMinimumServiceMonths()
                : 0;

        List<Employee> allEmployees = employeeRepository.findByOrganizationId(orgId);
        List<Appraisal> existingAppraisals = appraisalRepository.findByCycleId(cycleId);
        Set<Long> existingEmployeeIds = existingAppraisals.stream()
                .map(a -> a.getEmployee().getId())
                .collect(Collectors.toSet());

        List<Appraisal> createdAppraisals = new ArrayList<>();
        Organization org = cycle.getOrganization();

        for (Employee emp : allEmployees) {
            if (existingEmployeeIds.contains(emp.getId())) {
                continue;
            }

            // Check service duration
            if (minMonths > 0 && emp.getJoiningDate() != null) {
                long months = ChronoUnit.MONTHS.between(emp.getJoiningDate(), cycle.getStartDate());
                if (months < minMonths) {
                    continue;
                }
            }

            // Check department filter
            if (criteria != null && criteria.getDepartment() != null && !criteria.getDepartment().isBlank()) {
                if (emp.getDepartment() == null || !criteria.getDepartment().equalsIgnoreCase(emp.getDepartment())) {
                    continue;
                }
            }

            // Check designation filter
            if (criteria != null && criteria.getDesignation() != null && !criteria.getDesignation().isBlank()) {
                if (emp.getDesignation() == null || !criteria.getDesignation().equalsIgnoreCase(emp.getDesignation())) {
                    continue;
                }
            }

            Appraisal appraisal = new Appraisal();
            appraisal.setOrganization(org);
            appraisal.setCycle(cycle);
            appraisal.setEmployee(emp);
            appraisal.setStatus(AppraisalStatus.CREATED);
            appraisal.setCurrentStageOrder(1);
            appraisal.setCreatedAt(LocalDateTime.now());
            appraisal.setUpdatedAt(LocalDateTime.now());

            createdAppraisals.add(appraisal);
        }

        if (!createdAppraisals.isEmpty()) {
            appraisalRepository.saveAll(createdAppraisals);
        }

        cycle.setStatus("IN_PROGRESS");
        cycle.setUpdatedAt(LocalDateTime.now());
        cycleRepository.save(cycle);

        Map<String, Object> result = new HashMap<>();
        result.put("cycleId", cycleId);
        result.put("cycleName", cycle.getName());
        result.put("generatedCount", createdAppraisals.size());
        result.put("totalAppraisalsInCycle", existingAppraisals.size() + createdAppraisals.size());
        return result;
    }

    public AppraisalCycleResponseDto mapToDto(AppraisalCycle c) {
        AppraisalCycleResponseDto dto = new AppraisalCycleResponseDto();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setType(c.getType());
        dto.setStartDate(c.getStartDate());
        dto.setEndDate(c.getEndDate());
        dto.setStatus(c.getStatus());
        if (c.getCreatedBy() != null) {
            dto.setCreatedById(c.getCreatedBy().getId());
            dto.setCreatedByName(c.getCreatedBy().getFullName());
        }
        if (c.getEligibleCriteriaJson() != null) {
            try {
                dto.setEligibleEmployeeCriteria(objectMapper.readValue(c.getEligibleCriteriaJson(), CycleEligibilityCriteriaDto.class));
            } catch (Exception ignored) {}
        }
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}
