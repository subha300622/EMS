package com.example.ems.offboarding.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.offboarding.dto.OffboardingAnalyticsResponse;
import com.example.ems.offboarding.dto.OffboardingRequestSummaryDto;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.security.context.TenantContext;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class OffboardingDashboardService {

    @Autowired
    private OffboardingRepository offboardingRepository;

    /**
     * Determines the dashboard status category for a given Offboarding record.
     * Precedence:
     * 1. COMPLETED if status == "COMPLETED"
     * 2. SCHEDULED if not COMPLETED/REJECTED and (requestedLastWorkingDay > today OR exitDate > today)
     * 3. ACTIVE if status in (PENDING, IN_PROGRESS, APPROVED) and not SCHEDULED
     * 4. Otherwise returns the raw status (e.g., REJECTED, CANCELLED)
     */
    public String resolveDashboardStatus(Offboarding offboarding) {
        if (offboarding == null) return "UNKNOWN";
        String rawStatus = offboarding.getStatus() != null ? offboarding.getStatus().toUpperCase(Locale.ROOT) : "PENDING";
        
        if ("COMPLETED".equals(rawStatus)) {
            return "COMPLETED";
        }
        
        LocalDate today = LocalDate.now();
        LocalDate lastDate = offboarding.getRequestedLastWorkingDay() != null 
                ? offboarding.getRequestedLastWorkingDay() 
                : offboarding.getExitDate();

        if (!"REJECTED".equals(rawStatus) && !"CANCELLED".equals(rawStatus) && lastDate != null && lastDate.isAfter(today)) {
            return "SCHEDULED";
        }

        if ("PENDING".equals(rawStatus) || "IN_PROGRESS".equals(rawStatus) || "APPROVED".equals(rawStatus)) {
            return "ACTIVE";
        }

        return rawStatus;
    }

    /**
     * Retrieves a paginated list of offboarding requests for the current tenant.
     */
    @Transactional(readOnly = true)
    public Page<OffboardingRequestSummaryDto> getRequests(String statusFilter, String search, Pageable pageable) {
        Long organizationId = TenantContext.requireOrganizationId();
        return getRequests(organizationId, statusFilter, search, pageable);
    }

    /**
     * Retrieves a paginated list of offboarding requests scoped to the specified organization.
     */
    @Transactional(readOnly = true)
    public Page<OffboardingRequestSummaryDto> getRequests(Long organizationId, String statusFilter, String search, Pageable pageable) {
        Specification<Offboarding> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Mandatory Organization Scoping
            Join<Offboarding, Employee> employeeJoin = root.join("employee");
            predicates.add(cb.equal(employeeJoin.get("organization").get("id"), organizationId));

            // 2. Optional Search filter (fullName, employeeId code, or primary key ID)
            if (StringUtils.hasText(search)) {
                String searchPattern = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                Predicate nameMatch = cb.like(cb.lower(employeeJoin.get("fullName")), searchPattern);
                Predicate codeMatch = cb.like(cb.lower(employeeJoin.get("employeeId")), searchPattern);
                
                Predicate searchPredicate = cb.or(nameMatch, codeMatch);
                if (search.trim().matches("\\d+")) {
                    try {
                        Long idVal = Long.parseLong(search.trim());
                        Predicate idMatch = cb.equal(root.get("id"), idVal);
                        Predicate empIdMatch = cb.equal(employeeJoin.get("id"), idVal);
                        searchPredicate = cb.or(searchPredicate, idMatch, empIdMatch);
                    } catch (NumberFormatException ignored) {}
                }
                predicates.add(searchPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // If no dashboard status filter is requested, query with database pagination
        if (!StringUtils.hasText(statusFilter)) {
            Page<Offboarding> paged = offboardingRepository.findAll(spec, pageable);
            List<OffboardingRequestSummaryDto> dtos = paged.getContent().stream()
                    .map(this::mapToSummaryDto)
                    .collect(Collectors.toList());
            return new PageImpl<>(dtos, pageable, paged.getTotalElements());
        }

        // When dashboard status filter is provided (ACTIVE, COMPLETED, SCHEDULED),
        // fetch organization records and apply deterministic business categorization.
        List<Offboarding> allOrgRecords = offboardingRepository.findAll(spec);
        String targetFilter = statusFilter.trim().toUpperCase(Locale.ROOT);

        List<OffboardingRequestSummaryDto> filteredDtos = allOrgRecords.stream()
                .filter(ob -> targetFilter.equalsIgnoreCase(resolveDashboardStatus(ob)))
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredDtos.size());
        List<OffboardingRequestSummaryDto> pagedContent = (start <= end && start < filteredDtos.size()) 
                ? filteredDtos.subList(start, end) 
                : new ArrayList<>();

        return new PageImpl<>(pagedContent, pageable, filteredDtos.size());
    }

    /**
     * Computes aggregate offboarding analytics for the current tenant.
     */
    @Transactional(readOnly = true)
    public OffboardingAnalyticsResponse getAnalytics(LocalDate from, LocalDate to) {
        Long organizationId = TenantContext.requireOrganizationId();
        return getAnalytics(organizationId, from, to);
    }

    /**
     * Computes organization-level aggregate offboarding analytics and KPI metrics.
     */
    @Transactional(readOnly = true)
    public OffboardingAnalyticsResponse getAnalytics(Long organizationId, LocalDate from, LocalDate to) {
        List<Offboarding> records = offboardingRepository.findByEmployeeOrganizationId(organizationId);

        if (records == null || records.isEmpty()) {
            return new OffboardingAnalyticsResponse();
        }

        // Apply date filtering if specified (based on resignationDate, exitDate, or createdAt)
        List<Offboarding> filtered = records.stream().filter(ob -> {
            LocalDate recordDate = ob.getResignationDate() != null 
                    ? ob.getResignationDate() 
                    : (ob.getCreatedAt() != null ? ob.getCreatedAt().toLocalDate() : null);
            if (recordDate == null) return true;
            if (from != null && recordDate.isBefore(from)) return false;
            if (to != null && recordDate.isAfter(to)) return false;
            return true;
        }).collect(Collectors.toList());

        long activeCount = 0;
        long completedCount = 0;
        long scheduledCount = 0;
        long voluntaryCount = 0;
        long involuntaryCount = 0;

        List<Long> noticePeriodDaysList = new ArrayList<>();
        List<Long> completionDaysList = new ArrayList<>();

        for (Offboarding ob : filtered) {
            String dashStatus = resolveDashboardStatus(ob);
            switch (dashStatus) {
                case "ACTIVE" -> activeCount++;
                case "COMPLETED" -> completedCount++;
                case "SCHEDULED" -> scheduledCount++;
            }

            // Voluntary vs Involuntary classification
            String category = ob.getReasonCategory() != null ? ob.getReasonCategory().toUpperCase(Locale.ROOT) : "";
            if (category.contains("TERMINAT") || category.contains("INVOLUNTARY") || category.contains("PERFORMANCE") || category.contains("LAYOFF")) {
                involuntaryCount++;
            } else {
                voluntaryCount++;
            }

            // Notice period calculation: resignationDate -> lastWorkingDay/exitDate
            LocalDate resignDate = ob.getResignationDate();
            LocalDate lastDate = ob.getRequestedLastWorkingDay() != null ? ob.getRequestedLastWorkingDay() : ob.getExitDate();
            if (resignDate != null && lastDate != null && !lastDate.isBefore(resignDate)) {
                long days = ChronoUnit.DAYS.between(resignDate, lastDate);
                noticePeriodDaysList.add(days);
            }

            // Completion duration calculation for completed exits
            if ("COMPLETED".equals(ob.getStatus()) && ob.getCreatedAt() != null && ob.getUpdatedAt() != null) {
                long days = ChronoUnit.DAYS.between(ob.getCreatedAt().toLocalDate(), ob.getUpdatedAt().toLocalDate());
                completionDaysList.add(Math.max(0, days));
            }
        }

        double avgNoticePeriod = noticePeriodDaysList.isEmpty() ? 0.0 
                : noticePeriodDaysList.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double avgCompletionDays = completionDaysList.isEmpty() ? 0.0 
                : completionDaysList.stream().mapToLong(Long::longValue).average().orElse(0.0);

        return new OffboardingAnalyticsResponse(
                activeCount,
                completedCount,
                scheduledCount,
                filtered.size(),
                voluntaryCount,
                involuntaryCount,
                Math.round(avgNoticePeriod * 10.0) / 10.0,
                Math.round(avgCompletionDays * 10.0) / 10.0
        );
    }

    private OffboardingRequestSummaryDto mapToSummaryDto(Offboarding ob) {
        Employee emp = ob.getEmployee();
        Long empId = emp != null ? emp.getId() : null;
        String empName = emp != null ? emp.getFullName() : null;
        String empCode = emp != null ? emp.getEmployeeId() : null;
        String designation = emp != null ? emp.getDesignation() : null;
        String department = emp != null ? emp.getDepartment() : null;

        String exitType = ob.getReasonCategory() != null ? ob.getReasonCategory() : "RESIGNATION";
        String status = resolveDashboardStatus(ob);
        LocalDate resignationDate = ob.getResignationDate();
        LocalDate lastWorkingDate = ob.getRequestedLastWorkingDay() != null ? ob.getRequestedLastWorkingDay() : ob.getExitDate();
        String currentStage = ob.getCurrentStage() != null ? ob.getCurrentStage() : "INITIATED";

        return new OffboardingRequestSummaryDto(
                ob.getId(),
                empId,
                empName,
                empCode,
                designation,
                department,
                exitType,
                status,
                resignationDate,
                lastWorkingDate,
                currentStage
        );
    }
}
