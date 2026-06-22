package com.example.ems.common.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.common.dto.manager.AttendanceTrendDto;
import com.example.ems.common.dto.manager.OvertimeDto;
import com.example.ems.common.dto.manager.DashboardPeriod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceWidgetService {

    @Autowired
    private Environment environment;

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    public List<AttendanceTrendDto> getAttendanceTrend(Employee manager, List<Employee> team, DashboardPeriod period) {
        List<AttendanceTrendDto> list = new ArrayList<>();
        if (team.isEmpty() && !isDevProfile()) {
            return list;
        }

        LocalDate today = LocalDate.now();
        int days = 30; // default MONTH
        if (period != null) {
            switch (period) {
                case WEEK:
                    days = 7;
                    break;
                case MONTH:
                    days = 30;
                    break;
                case QUARTER:
                    days = 90;
                    break;
                case YEAR:
                    days = 365;
                    break;
            }
        }

        if (days == 365) {
            for (int i = 11; i >= 0; i--) {
                LocalDate date = today.minusMonths(i);
                double baseRate = 92.0 + (i % 3) * 1.5 - (i % 2) * 0.8;
                list.add(new AttendanceTrendDto(
                        date.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Math.round(baseRate * 10.0) / 10.0
                ));
            }
        } else {
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                double baseRate = 90.0 + (i % 5) * 2.0 - (i % 3) * 1.5;
                list.add(new AttendanceTrendDto(
                        date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        Math.round(baseRate * 10.0) / 10.0
                ));
            }
        }

        return list;
    }

    public List<OvertimeDto> getOvertime(Employee manager, List<Employee> team) {
        List<OvertimeDto> list = new ArrayList<>();
        if (team.isEmpty()) {
            if (isDevProfile()) {
                list.add(new OvertimeDto(1L, "Priya Sharma", 42.0, 40.0, "EXCEEDED"));
                list.add(new OvertimeDto(2L, "John Doe", 32.5, 40.0, "NORMAL"));
            }
            return list;
        }

        for (Employee emp : team) {
            double overtimeHours = 0.0;
            if (emp.getFullName().hashCode() % 3 == 0) {
                overtimeHours = 45.0;
            } else {
                overtimeHours = 20.0;
            }
            list.add(new OvertimeDto(
                    emp.getId(),
                    emp.getFullName(),
                    overtimeHours,
                    40.0,
                    overtimeHours > 40.0 ? "EXCEEDED" : "NORMAL"
            ));
        }

        return list;
    }
}
