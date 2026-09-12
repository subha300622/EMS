package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.*;

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
import com.example.ems.appraisal.entity.AppraisalConfigurationVersion;
import com.example.ems.appraisal.entity.AppraisalInitiationMode;
import com.example.ems.appraisal.repository.AppraisalConfigurationRepository;
import com.example.ems.appraisal.repository.AppraisalConfigurationVersionRepository;
import com.example.ems.common.exception.ConflictException;

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
    private AppraisalConfigurationRepository configRepository;

    @Autowired
    private AppraisalConfigurationExtendedService configExtendedService;

    @Autowired
    private AppraisalConfigurationVersionRepository versionRepository;

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
            throw new ConflictException("An appraisal cycle with name '" + dto.getName().trim() + "' already exists.");
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

    @Transactional
    public AppraisalCycleResponseDto updateCycle(Long cycleId, UpdateAppraisalCycleDto dto) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        if (!"DRAFT".equalsIgnoreCase(cycle.getStatus())) {
            throw new IllegalStateException("Cannot update appraisal cycle with status '" + cycle.getStatus() + "'. Only DRAFT cycles can be updated.");
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException("Cycle endDate cannot be before startDate");
        }

        if (dto.getName() != null && !dto.getName().trim().equalsIgnoreCase(cycle.getName())) {
            if (cycleRepository.findByOrganizationId(orgId).stream()
                    .anyMatch(c -> !c.getId().equals(cycleId) && c.getName().equalsIgnoreCase(dto.getName().trim()))) {
                throw new ConflictException("An appraisal cycle with name '" + dto.getName().trim() + "' already exists.");
            }
            cycle.setName(dto.getName().trim());
        }

        if (dto.getType() != null) {
            cycle.setType(dto.getType().toUpperCase());
        }
        if (dto.getStartDate() != null) {
            cycle.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            cycle.setEndDate(dto.getEndDate());
        }
        if (dto.getEligibleEmployeeCriteria() != null) {
            try {
                cycle.setEligibleCriteriaJson(objectMapper.writeValueAsString(dto.getEligibleEmployeeCriteria()));
            } catch (Exception ignored) {}
        }

        cycle.setUpdatedAt(LocalDateTime.now());
        return mapToDto(cycleRepository.save(cycle));
    }

    @Transactional
    public void deleteCycle(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        if (!"DRAFT".equalsIgnoreCase(cycle.getStatus())) {
            throw new IllegalStateException("Cannot delete appraisal cycle with status '" + cycle.getStatus() + "'. Only DRAFT cycles can be deleted.");
        }

        cycleRepository.delete(cycle);
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

        if ("OPEN".equalsIgnoreCase(cycle.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(cycle.getStatus())) {
            return mapToDto(cycle);
        }

        // Bind or snapshot configuration version if not present
        if (cycle.getConfigurationVersion() == null) {
            AppraisalConfigurationVersion latestVer = versionRepository
                    .findFirstByOrganizationIdOrderByVersionNumberDesc(orgId)
                    .orElseGet(() -> {
                        // Create initial snapshot if none exists
                        try {
                            configExtendedService.createSnapshot(cycle.getCreatedBy(), "Auto-snapshot upon activating cycle: " + cycle.getName());
                            return versionRepository.findFirstByOrganizationIdOrderByVersionNumberDesc(orgId).orElse(null);
                        } catch (Exception e) {
                            return null;
                        }
                    });
            if (latestVer != null) {
                cycle.setConfigurationVersion(latestVer);
            }
        }

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

    @Transactional(readOnly = true)
    public AppraisalConfigurationSnapshotDto getCycleConfiguration(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        if (cycle.getConfigurationVersion() != null && cycle.getConfigurationVersion().getSnapshotJson() != null) {
            try {
                return objectMapper.readValue(cycle.getConfigurationVersion().getSnapshotJson(), AppraisalConfigurationSnapshotDto.class);
            } catch (Exception ignored) {}
        }

        return configExtendedService.buildLiveSnapshot(1);
    }

    @Transactional(readOnly = true)
    public CycleEligibilityPreviewResponseDto getEligibilityPreview(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        CycleEligibilityCriteriaDto criteria = null;
        if (cycle.getEligibleCriteriaJson() != null) {
            try {
                criteria = objectMapper.readValue(cycle.getEligibleCriteriaJson(), CycleEligibilityCriteriaDto.class);
            } catch (Exception ignored) {}
        }

        int minMonths = (criteria != null && criteria.getMinimumServiceMonths() != null) ? criteria.getMinimumServiceMonths() : 0;
        List<Employee> allEmployees = employeeRepository.findByOrganizationId(orgId);
        List<Appraisal> existingAppraisals = appraisalRepository.findByCycleId(cycleId);
        Set<Long> existingEmployeeIds = existingAppraisals.stream()
                .map(a -> a.getEmployee().getId())
                .collect(Collectors.toSet());

        List<EmployeeEligibilityDetailDto> eligible = new ArrayList<>();
        List<EmployeeEligibilityDetailDto> ineligible = new ArrayList<>();

        for (Employee emp : allEmployees) {
            EmployeeEligibilityDetailDto detail = new EmployeeEligibilityDetailDto();
            detail.setEmployeeId(emp.getId());
            detail.setEmployeeName(emp.getFullName());
            detail.setDepartment(emp.getDepartment());
            detail.setDesignation(emp.getDesignation());
            detail.setJoiningDate(emp.getJoiningDate());

            if (emp.getJoiningDate() != null && cycle.getStartDate() != null) {
                long months = ChronoUnit.MONTHS.between(emp.getJoiningDate(), cycle.getStartDate());
                detail.setServiceMonths((int) Math.max(0, months));
            } else {
                detail.setServiceMonths(0);
            }

            if (existingEmployeeIds.contains(emp.getId())) {
                detail.setEligible(false);
                detail.setIneligibilityReason("Already has an appraisal in this cycle");
                ineligible.add(detail);
                continue;
            }

            if (minMonths > 0 && emp.getJoiningDate() != null) {
                long months = ChronoUnit.MONTHS.between(emp.getJoiningDate(), cycle.getStartDate());
                if (months < minMonths) {
                    detail.setEligible(false);
                    detail.setIneligibilityReason("Service duration (" + months + " months) is less than required (" + minMonths + " months)");
                    ineligible.add(detail);
                    continue;
                }
            }

            if (criteria != null && criteria.getDepartment() != null && !criteria.getDepartment().isBlank()) {
                if (emp.getDepartment() == null || !criteria.getDepartment().equalsIgnoreCase(emp.getDepartment())) {
                    detail.setEligible(false);
                    detail.setIneligibilityReason("Department '" + emp.getDepartment() + "' does not match criteria '" + criteria.getDepartment() + "'");
                    ineligible.add(detail);
                    continue;
                }
            }

            if (criteria != null && criteria.getDesignation() != null && !criteria.getDesignation().isBlank()) {
                if (emp.getDesignation() == null || !criteria.getDesignation().equalsIgnoreCase(emp.getDesignation())) {
                    detail.setEligible(false);
                    detail.setIneligibilityReason("Designation '" + emp.getDesignation() + "' does not match criteria '" + criteria.getDesignation() + "'");
                    ineligible.add(detail);
                    continue;
                }
            }

            detail.setEligible(true);
            eligible.add(detail);
        }

        CycleEligibilityPreviewResponseDto preview = new CycleEligibilityPreviewResponseDto();
        preview.setCycleId(cycle.getId());
        preview.setCycleName(cycle.getName());
        preview.setTotalEmployees(allEmployees.size());
        preview.setEligibleCount(eligible.size());
        preview.setIneligibleCount(ineligible.size());
        preview.setEligibleEmployees(eligible);
        preview.setIneligibleEmployees(ineligible);
        return preview;
    }

    @Transactional(readOnly = true)
    public CycleGenerationStatusResponseDto getGenerationStatus(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + cycleId));

        CycleEligibilityPreviewResponseDto preview = getEligibilityPreview(cycleId);
        List<Appraisal> existing = appraisalRepository.findByCycleId(cycleId);

        CycleGenerationStatusResponseDto status = new CycleGenerationStatusResponseDto();
        status.setCycleId(cycle.getId());
        status.setCycleName(cycle.getName());
        status.setCycleStatus(cycle.getStatus());
        status.setTotalEligibleEmployees(preview.getEligibleCount() + existing.size());
        status.setTotalGeneratedAppraisals(existing.size());
        status.setTotalPendingGeneration(preview.getEligibleCount());
        status.setGenerationComplete(preview.getEligibleCount() == 0);
        status.setCheckedAt(LocalDateTime.now());
        return status;
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
            if (config.getInitiationMode() == AppraisalInitiationMode.EMPLOYEE_ONLY) {
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

