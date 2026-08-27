package com.example.ems.onboarding.service;

import com.example.ems.onboarding.dto.report.OnboardingDashboardSummaryResponse;
import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OnboardingReportService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    public OnboardingDashboardSummaryResponse getDashboardSummary() {
        List<Onboarding> all = onboardingRepository.findAll();

        OnboardingDashboardSummaryResponse summary = new OnboardingDashboardSummaryResponse();
        summary.setTotal(all.size());
        summary.setPreJoining(all.stream().filter(o -> "PRE_JOINING".equalsIgnoreCase(o.getStatus()) || "INITIATED".equalsIgnoreCase(o.getStatus())).count());
        summary.setInProgress(all.stream().filter(o -> "IN_PROGRESS".equalsIgnoreCase(o.getStatus())).count());
        summary.setPendingApproval(all.stream().filter(o -> "PENDING_APPROVAL".equalsIgnoreCase(o.getStatus())).count());
        summary.setApproved(all.stream().filter(o -> "APPROVED".equalsIgnoreCase(o.getStatus())).count());
        summary.setCompleted(all.stream().filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus())).count());
        summary.setCancelled(all.stream().filter(o -> "CANCELLED".equalsIgnoreCase(o.getStatus())).count());
        summary.setOnHold(0);

        long overdueCount = all.stream().filter(o -> {
            if ("COMPLETED".equalsIgnoreCase(o.getStatus()) || "CANCELLED".equalsIgnoreCase(o.getStatus())) return false;
            return o.getJoiningDate() != null && o.getJoiningDate().isBefore(LocalDate.now());
        }).count();
        summary.setOverdue(overdueCount);

        return summary;
    }

    public List<Map<String, Object>> getCompletionReport(LocalDate fromDate, LocalDate toDate) {
        return onboardingRepository.findAll().stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .filter(o -> fromDate == null || (o.getCompletionDate() != null && !o.getCompletionDate().isBefore(fromDate)))
                .filter(o -> toDate == null || (o.getCompletionDate() != null && !o.getCompletionDate().isAfter(toDate)))
                .map(o -> Map.of(
                        "onboardingId", (Object) o.getId(),
                        "employeeId", o.getEmployee().getEmployeeId(),
                        "joiningDate", String.valueOf(o.getJoiningDate()),
                        "completionDate", String.valueOf(o.getCompletionDate()),
                        "progressPercentage", o.getProgress()
                )).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getOverdueReport(Long departmentId) {
        return onboardingRepository.findAll().stream()
                .filter(o -> !"COMPLETED".equalsIgnoreCase(o.getStatus()) && !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .filter(o -> o.getJoiningDate() != null && o.getJoiningDate().isBefore(LocalDate.now()))
                .map(o -> Map.of(
                        "onboardingId", (Object) o.getId(),
                        "employeeId", o.getEmployee().getEmployeeId(),
                        "status", o.getStatus(),
                        "joiningDate", String.valueOf(o.getJoiningDate()),
                        "progressPercentage", o.getProgress()
                )).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getUpcomingReport(LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate != null ? fromDate : LocalDate.now();
        LocalDate end = toDate != null ? toDate : start.plusDays(30);

        return onboardingRepository.findAll().stream()
                .filter(o -> o.getJoiningDate() != null && !o.getJoiningDate().isBefore(start) && !o.getJoiningDate().isAfter(end))
                .map(o -> Map.of(
                        "onboardingId", (Object) o.getId(),
                        "employeeId", o.getEmployee().getEmployeeId(),
                        "status", o.getStatus(),
                        "joiningDate", String.valueOf(o.getJoiningDate())
                )).collect(Collectors.toList());
    }

    public String exportReportsToCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("OnboardingId,EmployeeId,Status,JoiningDate,ProgressPercentage\n");
        for (Onboarding o : onboardingRepository.findAll()) {
            sb.append(o.getId()).append(",")
                    .append(o.getEmployee().getEmployeeId()).append(",")
                    .append(o.getStatus()).append(",")
                    .append(o.getJoiningDate()).append(",")
                    .append(o.getProgress()).append("\n");
        }
        return sb.toString();
    }
}
