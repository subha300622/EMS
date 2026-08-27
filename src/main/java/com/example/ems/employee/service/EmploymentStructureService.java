package com.example.ems.employee.service;

import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.dto.EmploymentStructureDtos;
import com.example.ems.employee.entity.Designation;
import com.example.ems.employee.entity.EmploymentType;
import com.example.ems.employee.entity.JobLevel;
import com.example.ems.employee.repository.DesignationRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.EmploymentTypeRepository;
import com.example.ems.employee.repository.JobLevelRepository;
import com.example.ems.employee.util.EmploymentStructureIdResolver;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmploymentStructureService {

    @Autowired
    private DesignationRepository designationRepository;

    @Autowired
    private JobLevelRepository jobLevelRepository;

    @Autowired
    private EmploymentTypeRepository employmentTypeRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    @Transactional(readOnly = true)
    public EmploymentStructureDtos.EmploymentStructurePageResponse listStructures(int page, int size, String search,
            String status) {
        if (page < 0)
            page = 0;
        if (size <= 0)
            size = 20;

        Long organizationId = TenantContext.requireOrganizationId();
        List<Designation> allDesignations = designationRepository.findByOrganizationId(organizationId);

        final String searchFilter = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;
        final String statusFilter = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : null;

        List<Designation> filtered = allDesignations.stream()
                .filter(des -> {
                    if (searchFilter != null) {
                        boolean matchName = des.getDesignation() != null
                                && des.getDesignation().toLowerCase().contains(searchFilter);
                        boolean matchDesc = des.getDescription() != null
                                && des.getDescription().toLowerCase().contains(searchFilter);
                        if (!matchName && !matchDesc)
                            return false;
                    }
                    if (statusFilter != null) {
                        String currentStatus = des.getStatus() != null ? des.getStatus().toUpperCase() : "ACTIVE";
                        if (!currentStatus.equals(statusFilter))
                            return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0)
            totalPages = 1;

        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);

        List<Designation> pagedList = filtered.subList(fromIndex, toIndex);

        List<EmploymentStructureDtos.EmploymentStructureSummaryResponse> content = pagedList.stream()
                .map(this::mapToSummaryResponse)
                .collect(Collectors.toList());

        return EmploymentStructureDtos.EmploymentStructurePageResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    @Transactional
    public EmploymentStructureDtos.EmploymentStructureResponse createStructure(
            EmploymentStructureDtos.CreateEmploymentStructureRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        if (request.getDesignation() == null || request.getDesignation().isBlank()) {
            throw new IllegalArgumentException("Designation name cannot be blank");
        }
        String trimmedName = request.getDesignation().trim();

        if (designationRepository.existsByDesignationIgnoreCaseAndOrganizationId(trimmedName, organizationId)) {
            throw new IllegalArgumentException("Designation '" + trimmedName + "' already exists");
        }

        Designation designation = new Designation();
        designation.setOrganization(organization);
        designation.setDesignation(trimmedName);
        designation.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        designation.setStatus("ACTIVE");

        if (request.getJobLevels() != null) {
            for (EmploymentStructureDtos.CreateJobLevelRequest jlReq : request.getJobLevels()) {
                if (jlReq.getJobLevel() == null || jlReq.getJobLevel().isBlank()) {
                    throw new IllegalArgumentException("Job Level name cannot be blank");
                }
                JobLevel jl = new JobLevel();
                jl.setOrganization(organization);
                jl.setJobLevel(jlReq.getJobLevel().trim());
                jl.setDescription(jlReq.getDescription() != null ? jlReq.getDescription().trim() : null);
                jl.setStatus("ACTIVE");

                if (jlReq.getEmploymentTypes() != null) {
                    for (EmploymentStructureDtos.CreateEmploymentTypeRequest etReq : jlReq.getEmploymentTypes()) {
                        if (etReq.getEmploymentType() == null || etReq.getEmploymentType().isBlank()) {
                            throw new IllegalArgumentException("Employment Type name cannot be blank");
                        }
                        EmploymentType et = new EmploymentType();
                        et.setOrganization(organization);
                        et.setEmploymentType(etReq.getEmploymentType().trim());
                        et.setDescription(etReq.getDescription() != null ? etReq.getDescription().trim() : null);
                        et.setStatus("ACTIVE");
                        jl.addEmploymentType(et);
                    }
                }
                designation.addJobLevel(jl);
            }
        }

        Designation saved = designationRepository.save(designation);
        return mapToFullResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmploymentStructureDtos.EmploymentStructureResponse getStructure(String designationIdStr) {
        Long organizationId = TenantContext.requireOrganizationId();
        Long id = EmploymentStructureIdResolver.parseDesignationId(designationIdStr);
        Designation designation = designationRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment structure not found with ID: " + designationIdStr));
        return mapToFullResponse(designation);
    }

    @Transactional
    public EmploymentStructureDtos.EmploymentStructureResponse editStructure(String designationIdStr,
            EmploymentStructureDtos.EditEmploymentStructureRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        Long designationId = EmploymentStructureIdResolver.parseDesignationId(designationIdStr);
        Designation designation = designationRepository.findByIdAndOrganizationId(designationId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment structure not found with ID: " + designationIdStr));

        if (request.getDesignation() == null || request.getDesignation().isBlank()) {
            throw new IllegalArgumentException("Designation name cannot be blank");
        }
        String trimmedName = request.getDesignation().trim();

        if (!designation.getDesignation().equalsIgnoreCase(trimmedName) &&
                designationRepository.existsByDesignationIgnoreCaseAndOrganizationId(trimmedName, organizationId)) {
            throw new IllegalArgumentException("Designation '" + trimmedName + "' already exists");
        }

        designation.setDesignation(trimmedName);
        designation.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);

        if (request.getJobLevels() != null) {
            Map<Long, JobLevel> existingJobLevels = designation.getJobLevels().stream()
                    .collect(Collectors.toMap(JobLevel::getId, jl -> jl));

            for (EmploymentStructureDtos.EditJobLevelRequest jlReq : request.getJobLevels()) {
                if (jlReq.getJobLevel() == null || jlReq.getJobLevel().isBlank()) {
                    throw new IllegalArgumentException("Job Level name cannot be blank");
                }

                JobLevel targetJl;
                if (jlReq.getJobLevelId() != null && !jlReq.getJobLevelId().isBlank()) {
                    Long jlId = EmploymentStructureIdResolver.parseJobLevelId(jlReq.getJobLevelId());
                    targetJl = existingJobLevels.get(jlId);
                    if (targetJl == null) {
                        throw new IllegalArgumentException("Job Level ID " + jlReq.getJobLevelId()
                                + " does not belong to designation " + designationIdStr);
                    }
                    targetJl.setJobLevel(jlReq.getJobLevel().trim());
                    targetJl.setDescription(jlReq.getDescription() != null ? jlReq.getDescription().trim() : null);
                } else {
                    targetJl = new JobLevel();
                    targetJl.setOrganization(organization);
                    targetJl.setJobLevel(jlReq.getJobLevel().trim());
                    targetJl.setDescription(jlReq.getDescription() != null ? jlReq.getDescription().trim() : null);
                    targetJl.setStatus("ACTIVE");
                    designation.addJobLevel(targetJl);
                }

                if (jlReq.getEmploymentTypes() != null) {
                    Map<Long, EmploymentType> existingEtMap = targetJl.getEmploymentTypes().stream()
                            .collect(Collectors.toMap(EmploymentType::getId, et -> et));

                    for (EmploymentStructureDtos.EditEmploymentTypeRequest etReq : jlReq.getEmploymentTypes()) {
                        if (etReq.getEmploymentType() == null || etReq.getEmploymentType().isBlank()) {
                            throw new IllegalArgumentException("Employment Type name cannot be blank");
                        }

                        if (etReq.getEmploymentTypeId() != null && !etReq.getEmploymentTypeId().isBlank()) {
                            Long etId = EmploymentStructureIdResolver
                                    .parseEmploymentTypeId(etReq.getEmploymentTypeId());
                            EmploymentType targetEt = existingEtMap.get(etId);
                            if (targetEt == null) {
                                throw new IllegalArgumentException("Employment Type ID " + etReq.getEmploymentTypeId()
                                        + " does not belong to Job Level " + jlReq.getJobLevelId());
                            }
                            targetEt.setEmploymentType(etReq.getEmploymentType().trim());
                            targetEt.setDescription(
                                    etReq.getDescription() != null ? etReq.getDescription().trim() : null);
                        } else {
                            EmploymentType newEt = new EmploymentType();
                            newEt.setOrganization(organization);
                            newEt.setEmploymentType(etReq.getEmploymentType().trim());
                            newEt.setDescription(etReq.getDescription() != null ? etReq.getDescription().trim() : null);
                            newEt.setStatus("ACTIVE");
                            targetJl.addEmploymentType(newEt);
                        }
                    }
                }
            }
        }

        Designation saved = designationRepository.save(designation);
        return mapToFullResponse(saved);
    }

    @Transactional
    public EmploymentStructureDtos.EmploymentStructureResponse updateStatus(String designationIdStr,
            EmploymentStructureDtos.StatusRequest request) {
        Long organizationId = TenantContext.requireOrganizationId();
        Long id = EmploymentStructureIdResolver.parseDesignationId(designationIdStr);
        Designation designation = designationRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment structure not found with ID: " + designationIdStr));

        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("Status cannot be blank");
        }
        String upperStatus = request.getStatus().trim().toUpperCase();
        if (!"ACTIVE".equals(upperStatus) && !"INACTIVE".equals(upperStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status: " + request.getStatus() + ". Valid values are ACTIVE, INACTIVE");
        }

        designation.setStatus(upperStatus);
        Designation saved = designationRepository.save(designation);

        return EmploymentStructureDtos.EmploymentStructureResponse.builder()
                .designationId(EmploymentStructureIdResolver.formatDesignationId(saved.getId()))
                .designation(saved.getDesignation())
                .status(saved.getStatus())
                .updatedAt(saved.getUpdatedAt() != null ? ISO_FORMATTER.format(saved.getUpdatedAt()) : null)
                .build();
    }

    @Transactional
    public EmploymentStructureDtos.EmploymentStructureDeleteResponse deleteStructure(String designationIdStr) {
        Long organizationId = TenantContext.requireOrganizationId();
        Long id = EmploymentStructureIdResolver.parseDesignationId(designationIdStr);
        Designation designation = designationRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employment structure not found with ID: " + designationIdStr));

        if (employeeRepository.existsByDesignationIgnoreCase(designation.getDesignation())) {
            throw new IllegalStateException(
                    "Employment structure cannot be deleted because it is assigned to employees");
        }

        designationRepository.delete(designation);

        return EmploymentStructureDtos.EmploymentStructureDeleteResponse.builder()
                .message("Employment structure deleted successfully")
                .designationId(EmploymentStructureIdResolver.formatDesignationId(id))
                .build();
    }

    @Transactional(readOnly = true)
    public void validateAssignment(String designationIdStr, String jobLevelIdStr, String employmentTypeIdStr) {
        Long orgId = TenantContext.requireOrganizationId();
        Long desId = EmploymentStructureIdResolver.parseDesignationId(designationIdStr);
        Long jlId = EmploymentStructureIdResolver.parseJobLevelId(jobLevelIdStr);
        Long etId = EmploymentStructureIdResolver.parseEmploymentTypeId(employmentTypeIdStr);

        Designation designation = designationRepository.findByIdAndOrganizationId(desId, orgId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Designation ID " + designationIdStr + " does not exist"));

        if (!"ACTIVE".equalsIgnoreCase(designation.getStatus())) {
            throw new IllegalArgumentException(
                    "Designation '" + designation.getDesignation() + "' is INACTIVE and cannot be assigned");
        }

        JobLevel jobLevel = jobLevelRepository.findByIdAndDesignationIdAndOrganizationId(jlId, desId, orgId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Job Level ID " + jobLevelIdStr + " does not belong to Designation " + designationIdStr));

        if (!"ACTIVE".equalsIgnoreCase(jobLevel.getStatus())) {
            throw new IllegalArgumentException(
                    "Job Level '" + jobLevel.getJobLevel() + "' is INACTIVE and cannot be assigned");
        }

        EmploymentType employmentType = employmentTypeRepository.findByIdAndJobLevelIdAndOrganizationId(etId, jlId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Employment Type ID " + employmentTypeIdStr
                        + " does not belong to Job Level " + jlId));

        if (!"ACTIVE".equalsIgnoreCase(employmentType.getStatus())) {
            throw new IllegalArgumentException(
                    "Employment Type '" + employmentType.getEmploymentType() + "' is INACTIVE and cannot be assigned");
        }
    }

    private EmploymentStructureDtos.EmploymentStructureSummaryResponse mapToSummaryResponse(Designation des) {
        int jlCount = des.getJobLevels() != null ? des.getJobLevels().size() : 0;
        int activeJlCount = des.getJobLevels() != null
                ? (int) des.getJobLevels().stream().filter(jl -> "ACTIVE".equalsIgnoreCase(jl.getStatus())).count()
                : 0;

        int etCount = des.getJobLevels() != null ? des.getJobLevels().stream()
                .mapToInt(jl -> jl.getEmploymentTypes() != null ? jl.getEmploymentTypes().size() : 0).sum() : 0;
        int activeEtCount = des
                .getJobLevels() != null ? des
                        .getJobLevels().stream()
                        .mapToInt(jl -> jl.getEmploymentTypes() != null
                                ? (int) jl.getEmploymentTypes().stream()
                                        .filter(et -> "ACTIVE".equalsIgnoreCase(et.getStatus())).count()
                                : 0)
                        .sum() : 0;

        int userCount = (int) employeeRepository.countByDesignationIgnoreCase(des.getDesignation());

        List<EmploymentStructureDtos.JobLevelResponse> jlResponses = des.getJobLevels() != null
                ? des.getJobLevels().stream().map(jl -> {
                    List<EmploymentStructureDtos.EmploymentTypeResponse> etResponses = jl.getEmploymentTypes() != null
                            ? jl.getEmploymentTypes().stream().map(et -> EmploymentStructureDtos.EmploymentTypeResponse
                                    .builder()
                                    .employmentTypeId(EmploymentStructureIdResolver.formatEmploymentTypeId(et.getId()))
                                    .employmentType(et.getEmploymentType())
                                    .description(et.getDescription())
                                    .status(et.getStatus() != null ? et.getStatus() : "ACTIVE")
                                    .build()).collect(Collectors.toList())
                            : Collections.emptyList();

                    return EmploymentStructureDtos.JobLevelResponse.builder()
                            .jobLevelId(EmploymentStructureIdResolver.formatJobLevelId(jl.getId()))
                            .jobLevel(jl.getJobLevel())
                            .description(jl.getDescription())
                            .status(jl.getStatus() != null ? jl.getStatus() : "ACTIVE")
                            .employmentTypes(etResponses)
                            .build();
                }).collect(Collectors.toList())
                : Collections.emptyList();

        return EmploymentStructureDtos.EmploymentStructureSummaryResponse.builder()
                .designationId(EmploymentStructureIdResolver.formatDesignationId(des.getId()))
                .designation(des.getDesignation())
                .description(des.getDescription())
                .status(des.getStatus() != null ? des.getStatus() : "ACTIVE")
                .jobLevels(jlResponses)
                .jobLevelCount(jlCount)
                .activeJobLevelCount(activeJlCount)
                .employmentTypeCount(etCount)
                .activeEmploymentTypeCount(activeEtCount)
                .userCount(userCount)
                .createdAt(des.getCreatedAt() != null ? ISO_FORMATTER.format(des.getCreatedAt()) : null)
                .updatedAt(des.getUpdatedAt() != null ? ISO_FORMATTER.format(des.getUpdatedAt())
                        : (des.getCreatedAt() != null ? ISO_FORMATTER.format(des.getCreatedAt()) : null))
                .build();
    }

    private EmploymentStructureDtos.EmploymentStructureResponse mapToFullResponse(Designation des) {
        int jlCount = des.getJobLevels() != null ? des.getJobLevels().size() : 0;
        int activeJlCount = des.getJobLevels() != null
                ? (int) des.getJobLevels().stream().filter(jl -> "ACTIVE".equalsIgnoreCase(jl.getStatus())).count()
                : 0;

        int etCount = des.getJobLevels() != null ? des.getJobLevels().stream()
                .mapToInt(jl -> jl.getEmploymentTypes() != null ? jl.getEmploymentTypes().size() : 0).sum() : 0;
        int activeEtCount = des
                .getJobLevels() != null ? des
                        .getJobLevels().stream()
                        .mapToInt(jl -> jl.getEmploymentTypes() != null
                                ? (int) jl.getEmploymentTypes().stream()
                                        .filter(et -> "ACTIVE".equalsIgnoreCase(et.getStatus())).count()
                                : 0)
                        .sum() : 0;

        int userCount = (int) employeeRepository.countByDesignationIgnoreCase(des.getDesignation());

        List<EmploymentStructureDtos.JobLevelResponse> jlResponses = des.getJobLevels() != null
                ? des.getJobLevels().stream().map(jl -> {
                    List<EmploymentStructureDtos.EmploymentTypeResponse> etResponses = jl.getEmploymentTypes() != null
                            ? jl.getEmploymentTypes().stream().map(et -> EmploymentStructureDtos.EmploymentTypeResponse
                                    .builder()
                                    .employmentTypeId(EmploymentStructureIdResolver.formatEmploymentTypeId(et.getId()))
                                    .employmentType(et.getEmploymentType())
                                    .description(et.getDescription())
                                    .status(et.getStatus() != null ? et.getStatus() : "ACTIVE")
                                    .build()).collect(Collectors.toList())
                            : Collections.emptyList();

                    return EmploymentStructureDtos.JobLevelResponse.builder()
                            .jobLevelId(EmploymentStructureIdResolver.formatJobLevelId(jl.getId()))
                            .jobLevel(jl.getJobLevel())
                            .description(jl.getDescription())
                            .status(jl.getStatus() != null ? jl.getStatus() : "ACTIVE")
                            .employmentTypes(etResponses)
                            .build();
                }).collect(Collectors.toList())
                : Collections.emptyList();

        return EmploymentStructureDtos.EmploymentStructureResponse.builder()
                .designationId(EmploymentStructureIdResolver.formatDesignationId(des.getId()))
                .designation(des.getDesignation())
                .description(des.getDescription())
                .status(des.getStatus() != null ? des.getStatus() : "ACTIVE")
                .jobLevelCount(jlCount)
                .activeJobLevelCount(activeJlCount)
                .employmentTypeCount(etCount)
                .activeEmploymentTypeCount(activeEtCount)
                .userCount(userCount)
                .jobLevels(jlResponses)
                .createdAt(des.getCreatedAt() != null ? ISO_FORMATTER.format(des.getCreatedAt()) : null)
                .updatedAt(des.getUpdatedAt() != null ? ISO_FORMATTER.format(des.getUpdatedAt())
                        : (des.getCreatedAt() != null ? ISO_FORMATTER.format(des.getCreatedAt()) : null))
                .build();
    }
}
