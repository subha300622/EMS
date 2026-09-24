package com.example.ems.employee.service.actioncenter;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalStatus;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.employee.dto.dashboard.EmployeeActionDto;
import com.example.ems.employee.dto.dashboard.EmployeeActionLinkDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.performance.entity.PerformanceReviewRecord;
import com.example.ems.performance.repository.PerformanceReviewRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PerformanceActionProvider implements EmployeeActionProvider {

    private static final Logger log = LoggerFactory.getLogger(PerformanceActionProvider.class);

    @Autowired
    private AppraisalRepository appraisalRepository;

    @Autowired
    private PerformanceReviewRecordRepository reviewRecordRepository;

    @Override
    public List<EmployeeActionDto> getActions(Employee employee, Long orgId) {
        if (employee == null || employee.getId() == null) {
            return Collections.emptyList();
        }

        List<EmployeeActionDto> actions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Set<Long> processedReviewIds = new HashSet<>();

        // 1. Check PerformanceReviewRecord (Enterprise Performance Reviews)
        try {
            List<PerformanceReviewRecord> records;
            if (orgId != null) {
                records = reviewRecordRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId());
            } else {
                records = reviewRecordRepository.findAll().stream()
                        .filter(r -> r.getEmployee() != null && employee.getId().equals(r.getEmployee().getId()))
                        .toList();
            }

            for (PerformanceReviewRecord rec : records) {
                if (rec.getSelfSubmittedAt() == null && !isFinishedOrCancelled(rec.getStatus())) {
                    processedReviewIds.add(rec.getId());
                    LocalDate due = null;
                    String cycleName = "Performance Appraisal";
                    if (rec.getCycle() != null) {
                        if (rec.getCycle().getName() != null) {
                            cycleName = rec.getCycle().getName();
                        }
                        if (rec.getCycle().getSelfReviewDeadline() != null) {
                            due = rec.getCycle().getSelfReviewDeadline();
                        } else if (rec.getCycle().getEndDate() != null) {
                            due = rec.getCycle().getEndDate();
                        }
                    }

                    String status = "PENDING";
                    String priority = "NORMAL";
                    if (due != null) {
                        if (due.isBefore(today)) {
                            status = "OVERDUE";
                            priority = "HIGH";
                        } else if (!due.isAfter(today.plusDays(7))) {
                            status = "DUE_SOON";
                        }
                    }

                    String title = (due != null)
                            ? "Complete self-review by " + due
                            : "Complete self-review";

                    actions.add(new EmployeeActionDto(
                            "APP-" + rec.getId(),
                            "SELF_REVIEW",
                            title,
                            status,
                            priority,
                            due != null ? due.toString() : null,
                            "Part of " + cycleName + " cycle",
                            new EmployeeActionLinkDto("View Task", "/employee/performance/self-reviews/" + rec.getId())
                    ));
                }
            }
        } catch (Exception ex) {
            log.warn("Error calculating enterprise performance review actions for employee #{}: {}", employee.getId(), ex.getMessage());
        }

        // 2. Check Appraisals (Traditional / Cycle Appraisals)
        try {
            List<Appraisal> appraisals;
            if (orgId != null) {
                appraisals = appraisalRepository.findByOrganizationIdAndEmployeeId(orgId, employee.getId());
            } else {
                appraisals = appraisalRepository.findByEmployeeId(employee.getId());
            }

            for (Appraisal app : appraisals) {
                if (processedReviewIds.contains(app.getId())) {
                    continue;
                }
                if (app.getSelfReviewSubmittedAt() == null && app.getSelfRating() == null
                        && app.getStatus() != AppraisalStatus.COMPLETED && app.getStatus() != AppraisalStatus.CANCELLED) {
                    LocalDate due = null;
                    String cycleName = "Performance Appraisal";
                    if (app.getCycle() != null) {
                        if (app.getCycle().getName() != null) {
                            cycleName = app.getCycle().getName();
                        }
                        if (app.getCycle().getEndDate() != null) {
                            due = app.getCycle().getEndDate();
                        }
                    }

                    String status = "PENDING";
                    String priority = "NORMAL";
                    if (due != null) {
                        if (due.isBefore(today)) {
                            status = "OVERDUE";
                            priority = "HIGH";
                        } else if (!due.isAfter(today.plusDays(7))) {
                            status = "DUE_SOON";
                        }
                    }

                    String title = (due != null)
                            ? "Complete self-review by " + due
                            : "Complete self-review";

                    actions.add(new EmployeeActionDto(
                            "APP-" + app.getId(),
                            "SELF_REVIEW",
                            title,
                            status,
                            priority,
                            due != null ? due.toString() : null,
                            "Part of " + cycleName + " cycle",
                            new EmployeeActionLinkDto("View Task", "/employee/performance/self-reviews/" + app.getId())
                    ));
                }
            }
        } catch (Exception ex) {
            log.warn("Error calculating appraisal actions for employee #{}: {}", employee.getId(), ex.getMessage());
        }

        return actions;
    }

    private boolean isFinishedOrCancelled(String status) {
        if (status == null) return false;
        String s = status.toUpperCase();
        return s.contains("COMPLETED") || s.contains("CANCELLED") || s.contains("CLOSED") || s.contains("FINALIZED");
    }
}
