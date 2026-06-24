package com.example.ems.schedule.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.schedule.dto.OvertimeMonitorDto;
import com.example.ems.schedule.dto.OvertimeSummaryDto;
import com.example.ems.schedule.entity.MyShiftTemplate;
import com.example.ems.schedule.repository.EmployeeShiftCountProjection;
import com.example.ems.schedule.repository.MyShiftRepository;
import com.example.ems.schedule.repository.MyShiftTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkforceTimeAnalyticsService {

    @Autowired
    private MyShiftRepository shiftRepository;

    @Autowired
    private MyShiftTemplateRepository templateRepository;

    public OvertimeSummaryDto calculateOvertimeAnalytics(List<Employee> employees, LocalDate startDate, LocalDate endDate) {
        if (employees == null || employees.isEmpty()) {
            return new OvertimeSummaryDto(0.0, Collections.emptyList());
        }

        List<Long> employeeIds = employees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        // Fetch shift templates
        List<MyShiftTemplate> templates = templateRepository.findAll();
        Map<Long, MyShiftTemplate> templateMap = templates.stream()
                .collect(Collectors.toMap(MyShiftTemplate::getId, t -> t, (t1, t2) -> t1));

        // Fetch aggregated shift counts from the database
        List<EmployeeShiftCountProjection> countProjections = shiftRepository
                .countShiftsByEmployeeAndTemplate(employeeIds, startDate, endDate);

        // Group worked hours by employee ID
        Map<Long, Double> workedHoursMap = new HashMap<>();
        for (EmployeeShiftCountProjection projection : countProjections) {
            Long empId = projection.getEmployeeId();
            Long tempId = projection.getTemplateId();
            Long count = projection.getShiftCount();

            if (empId != null && tempId != null && count != null) {
                MyShiftTemplate template = templateMap.get(tempId);
                double hoursPerShift = calculateShiftHours(template);
                double totalHoursForTemplate = count * hoursPerShift;

                workedHoursMap.put(empId, workedHoursMap.getOrDefault(empId, 0.0) + totalHoursForTemplate);
            }
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double threshold = (totalDays / 7.0) * 40.0;
        if (threshold <= 0.0) {
            threshold = 40.0;
        }

        List<OvertimeMonitorDto> result = new ArrayList<>();
        double totalOvertime = 0.0;

        for (Employee emp : employees) {
            double workedHours = workedHoursMap.getOrDefault(emp.getId(), 0.0);
            double maxHours = 15.0; // standard limit

            // Compute overtime hours
            double overtimeHours = Math.max(0.0, workedHours - threshold);

            workedHours = Math.round(workedHours * 10.0) / 10.0;
            overtimeHours = Math.round(overtimeHours * 10.0) / 10.0;

            String color = "green";
            if (overtimeHours > 8.0) {
                color = "orange";
            } else if (overtimeHours > 0.0) {
                color = "blue";
            }

            OvertimeMonitorDto dto = new OvertimeMonitorDto();
            dto.setEmployeeId(emp.getId());
            dto.setName(emp.getFullName());
            dto.setInitials(getInitials(emp.getFullName()));
            dto.setWorkedHours(workedHours);
            dto.setMaxHours(maxHours);
            dto.setOvertimeHours(overtimeHours);
            dto.setDisplayLabel((int) workedHours + "h / " + (int) maxHours + "h");
            dto.setColor(color);

            result.add(dto);
            totalOvertime += overtimeHours;
        }

        return new OvertimeSummaryDto(Math.round(totalOvertime * 10.0) / 10.0, result);
    }

    public String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.split("\\s+");
        return (parts[0].charAt(0) + "" +
                (parts.length > 1 ? parts[1].charAt(0) : ""))
                .toUpperCase();
    }

    private double calculateShiftHours(MyShiftTemplate template) {
        if (template == null || "NONE".equalsIgnoreCase(template.getName())) return 0.0;
        try {
            String[] startParts = template.getStartTime().split(":");
            String[] endParts = template.getEndTime().split(":");
            int startMin = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMin = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
            int durationMin = endMin - startMin;
            if (durationMin < 0) {
                durationMin += 24 * 60; // crossover midnight
            }
            int breakMin = template.getBreakDurationMinutes() != null ? template.getBreakDurationMinutes() : 0;
            int workingMin = durationMin - breakMin;
            return Math.max(0.0, workingMin / 60.0);
        } catch (Exception e) {
            return 8.0; // standard default
        }
    }
}
