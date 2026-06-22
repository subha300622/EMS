package com.example.ems.common.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.common.dto.manager.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NotificationWidgetService {

    @Autowired
    private Environment environment;

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    public List<NotificationDto> getNotifications(Employee manager, List<Employee> team) {
        List<NotificationDto> list = new ArrayList<>();
        if (team.isEmpty() && !isDevProfile()) {
            return list;
        }

        list.add(new NotificationDto(
                1L,
                "Leave Approval Pending",
                "Priya Sharma requested leave",
                "APPROVAL",
                "HIGH",
                false,
                "2026-05-20T10:00:00"
        ));
        return list;
    }

    public List<AlertDto> getAlerts(Employee manager, List<Employee> team) {
        List<AlertDto> list = new ArrayList<>();
        if (team.isEmpty() && !isDevProfile()) {
            return list;
        }

        list.add(new AlertDto("LEAVE_PENDING", "1 leave request needs approval"));
        list.add(new AlertDto("OVERTIME", "2 team members exceeded overtime limit"));
        return list;
    }

    public List<InsightDto> getInsights(Employee manager, List<Employee> team) {
        List<InsightDto> list = new ArrayList<>();
        if (team.isEmpty() && !isDevProfile()) {
            return list;
        }

        list.add(new InsightDto(InsightSeverity.HIGH, "3 employees below 80% attendance"));
        list.add(new InsightDto(InsightSeverity.MEDIUM, "2 pending leave approvals"));
        return list;
    }

    public List<QuickActionDto> getQuickActions(Employee manager, List<Employee> team) {
        return List.of(
                new QuickActionDto("APPROVE_LEAVE", "Approve Leave"),
                new QuickActionDto("ADD_SHIFT", "Add Shift")
        );
    }
}
