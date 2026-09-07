package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.AppraisalCycleDashboardDto;
import com.example.ems.appraisal.dto.AppraisalEmployeeDashboardDto;
import com.example.ems.appraisal.dto.AppraisalOrganizationDashboardDto;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.AppraisalReviewStageRepository;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppraisalDashboardService {

    @Autowired
    private AppraisalCycleRepository cycleRepository;

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private AppraisalReviewStageRepository stageRepository;

    @Autowired
    private IncrementRepository incrementRepository;

    @Transactional(readOnly = true)
    public AppraisalOrganizationDashboardDto getOrganizationDashboard() {
        Long orgId = TenantContext.requireOrganizationId();
        List<AppraisalCycle> cycles = cycleRepository.findByOrganizationId(orgId);
        List<Appraisal> appraisals = appraisalRepository.findByOrganizationId(orgId);
        List<Increment> increments = incrementRepository.findByOrganizationId(orgId);

        Optional<AppraisalCycle> activeCycle = cycles.stream()
                .filter(c -> "OPEN".equalsIgnoreCase(c.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(c.getStatus()))
                .findFirst();

        Map<String, Long> statusBreakdown = appraisals.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus() != null ? a.getStatus().name() : "UNKNOWN", Collectors.counting()));

        Map<String, Long> categoryDistribution = appraisals.stream()
                .filter(a -> a.getPerformanceCategory() != null)
                .collect(Collectors.groupingBy(Appraisal::getPerformanceCategory, Collectors.counting()));

        OptionalDouble avgRatingOpt = appraisals.stream()
                .filter(a -> a.getFinalRating() != null)
                .mapToDouble(Appraisal::getFinalRating)
                .average();

        Double avgRating = avgRatingOpt.isPresent() ? Math.round(avgRatingOpt.getAsDouble() * 100.0) / 100.0 : null;

        int approvedInc = (int) increments.stream().filter(i -> "APPROVED".equalsIgnoreCase(i.getStatus())).count();
        int appliedInc = (int) increments.stream().filter(i -> "APPLIED".equalsIgnoreCase(i.getStatus())).count();

        AppraisalOrganizationDashboardDto dto = new AppraisalOrganizationDashboardDto();
        dto.setTotalCycles(cycles.size());
        dto.setOpenCycles((int) cycles.stream().filter(c -> "OPEN".equalsIgnoreCase(c.getStatus()) || "IN_PROGRESS".equalsIgnoreCase(c.getStatus())).count());
        dto.setActiveCycleId(activeCycle.map(AppraisalCycle::getId).orElse(null));
        dto.setActiveCycleName(activeCycle.map(AppraisalCycle::getName).orElse(null));
        dto.setTotalAppraisals(appraisals.size());
        dto.setStatusBreakdown(statusBreakdown);
        dto.setAverageRating(avgRating);
        dto.setPerformanceCategoryDistribution(categoryDistribution);
        dto.setTotalIncrementsApproved(approvedInc);
        dto.setTotalIncrementsApplied(appliedInc);

        return dto;
    }

    @Transactional(readOnly = true)
    public AppraisalCycleDashboardDto getCycleDashboard(Long cycleId) {
        Long orgId = TenantContext.requireOrganizationId();
        AppraisalCycle cycle = cycleRepository.findByIdAndOrganizationId(cycleId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal cycle not found with ID: " + cycleId));

        List<Appraisal> appraisals = appraisalRepository.findByCycleId(cycleId);
        int total = appraisals.size();

        int completed = (int) appraisals.stream().filter(a -> a.getStatus() == AppraisalStatus.COMPLETED || a.getStatus() == AppraisalStatus.PUBLISHED).count();
        int published = (int) appraisals.stream().filter(a -> a.getStatus() == AppraisalStatus.PUBLISHED).count();
        double completionPct = total > 0 ? Math.round(((double) completed / total * 100.0) * 100.0) / 100.0 : 0.0;

        Map<String, Long> statusBreakdown = appraisals.stream()
                .collect(Collectors.groupingBy(a -> a.getStatus() != null ? a.getStatus().name() : "UNKNOWN", Collectors.counting()));

        Map<String, Long> stageBreakdown = appraisals.stream()
                .collect(Collectors.groupingBy(a -> "Stage " + (a.getCurrentStageOrder() != null ? a.getCurrentStageOrder() : 1), Collectors.counting()));

        Map<String, Long> categoryDistribution = appraisals.stream()
                .filter(a -> a.getPerformanceCategory() != null)
                .collect(Collectors.groupingBy(Appraisal::getPerformanceCategory, Collectors.counting()));

        OptionalDouble avgRatingOpt = appraisals.stream()
                .filter(a -> a.getFinalRating() != null)
                .mapToDouble(Appraisal::getFinalRating)
                .average();

        Double avgRating = avgRatingOpt.isPresent() ? Math.round(avgRatingOpt.getAsDouble() * 100.0) / 100.0 : null;

        AppraisalCycleDashboardDto dto = new AppraisalCycleDashboardDto();
        dto.setCycleId(cycle.getId());
        dto.setCycleName(cycle.getName());
        dto.setCycleStatus(cycle.getStatus());
        dto.setTotalAppraisals(total);
        dto.setCompletedCount(completed);
        dto.setPublishedCount(published);
        dto.setCompletionPercentage(completionPct);
        dto.setAverageRating(avgRating);
        dto.setStatusBreakdown(statusBreakdown);
        dto.setStageBreakdown(stageBreakdown);
        dto.setCategoryDistribution(categoryDistribution);

        return dto;
    }

    @Transactional(readOnly = true)
    public AppraisalEmployeeDashboardDto getEmployeeDashboard(Employee employee) {
        Long orgId = TenantContext.requireOrganizationId();
        List<Appraisal> appraisals = appraisalRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId());

        Appraisal latestAppraisal = appraisals.isEmpty() ? null : appraisals.get(appraisals.size() - 1);
        List<AppraisalReviewStage> stages = stageRepository.findByOrganizationIdOrderByStageOrderAsc(orgId);

        String stageName = null;
        if (latestAppraisal != null && latestAppraisal.getCurrentStageOrder() != null) {
            stageName = stages.stream()
                    .filter(s -> s.getStageOrder().equals(latestAppraisal.getCurrentStageOrder()))
                    .map(AppraisalReviewStage::getStageName)
                    .findFirst()
                    .orElse("Stage " + latestAppraisal.getCurrentStageOrder());
        }

        Optional<Increment> incOpt = latestAppraisal != null
                ? incrementRepository.findByAppraisalId(latestAppraisal.getId())
                : Optional.empty();

        AppraisalEmployeeDashboardDto dto = new AppraisalEmployeeDashboardDto();
        dto.setEmployeeId(employee.getId());
        dto.setEmployeeName(employee.getFullName());
        if (latestAppraisal != null) {
            dto.setCurrentAppraisalId(latestAppraisal.getId());
            dto.setCurrentAppraisalStatus(latestAppraisal.getStatus() != null ? latestAppraisal.getStatus().name() : "NONE");
            dto.setCurrentStageOrder(latestAppraisal.getCurrentStageOrder());
            dto.setCurrentStageName(stageName);
            dto.setSelfRating(latestAppraisal.getSelfRating());
            dto.setFinalRating(latestAppraisal.getFinalRating());
            dto.setPerformanceCategory(latestAppraisal.getPerformanceCategory());
            dto.setPublishedAt(latestAppraisal.getPublishedAt());
        }
        if (incOpt.isPresent()) {
            Increment inc = incOpt.get();
            dto.setApprovedIncrementPercentage(inc.getIncrementPercentage() != null ? inc.getIncrementPercentage().doubleValue() : null);
            dto.setIncrementStatus(inc.getStatus());
        }
        dto.setTotalHistoricalAppraisals(appraisals.size());

        return dto;
    }
}
