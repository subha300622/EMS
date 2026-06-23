package com.example.ems.schedule.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.leave.entity.Leave;
import com.example.ems.schedule.dto.DailyShiftDto;
import com.example.ems.schedule.dto.EmployeeScheduleGridDto;
import com.example.ems.schedule.entity.MyShift;
import com.example.ems.schedule.entity.MyShiftTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ScheduleGridBuilder {

    public List<EmployeeScheduleGridDto> buildGrid(List<Employee> employees, List<MyShift> shifts, List<Leave> leaves, LocalDate startDate, LocalDate endDate) {
        List<EmployeeScheduleGridDto> grid = new ArrayList<>();

        for (Employee emp : employees) {
            EmployeeScheduleGridDto row = new EmployeeScheduleGridDto();
            row.setEmployeeId(emp.getId());
            row.setName(emp.getFullName());
            row.setDesignation(emp.getDesignation());
            row.setDepartment(emp.getDepartment());
            row.setAvatar(emp.getProfileImage() != null && !emp.getProfileImage().isBlank() ? emp.getProfileImage() : "https://api.dicebear.com/7.x/initials/svg?seed=" + java.net.URLEncoder.encode(emp.getFullName(), java.nio.charset.StandardCharsets.UTF_8));

            List<DailyShiftDto> dailyShifts = new ArrayList<>();
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                final LocalDate currentDate = date;

                // 1. Check leave override
                Optional<Leave> activeLeave = leaves.stream()
                        .filter(l -> l.getEmployee() != null && l.getEmployee().getId().equals(emp.getId()))
                        .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()))
                        .filter(l -> !currentDate.isBefore(l.getStartDate()) && !currentDate.isAfter(l.getEndDate()))
                        .findFirst();

                if (activeLeave.isPresent()) {
                    dailyShifts.add(new DailyShiftDto(
                            currentDate,
                            null,
                            "LEAVE",
                            "On Approved Leave",
                            null,
                            "LEAVE"
                    ));
                    continue;
                }

                // 2. Check assigned shifts
                Optional<MyShift> activeShift = shifts.stream()
                        .filter(s -> s.getEmployee() != null && s.getEmployee().getId().equals(emp.getId()))
                        .filter(s -> s.getDate().isEqual(currentDate))
                        .findFirst();

                if (activeShift.isPresent()) {
                    MyShift shift = activeShift.get();
                    MyShiftTemplate temp = shift.getTemplate();
                    String type = "NONE";
                    String label = "None";
                    String timeRange = null;

                    if (temp != null) {
                        timeRange = temp.getStartTime() + " - " + temp.getEndTime();
                        if (temp.getId() == 101L || "GENERAL_SHIFT".equalsIgnoreCase(temp.getName())) {
                            type = "FULL_DAY";
                            label = "Full Day";
                        } else if (temp.getId() == 102L || "EVENING_SHIFT".equalsIgnoreCase(temp.getName())) {
                            type = "EVENING";
                            label = "Evening";
                        } else if (temp.getId() == 103L || "NIGHT_SHIFT".equalsIgnoreCase(temp.getName())) {
                            type = "NIGHT";
                            label = "Night";
                        } else if (temp.getId() == 104L || "MORNING_SHIFT".equalsIgnoreCase(temp.getName())) {
                            type = "MORNING";
                            label = "Morning";
                        } else {
                            type = temp.getName();
                            label = temp.getName();
                        }
                    }

                    String status = shift.getStatus();
                    if (status == null) {
                        status = "ASSIGNED";
                    }

                    dailyShifts.add(new DailyShiftDto(
                            currentDate,
                            shift.getId(),
                            type,
                            label,
                            timeRange,
                            status
                    ));
                } else {
                    dailyShifts.add(new DailyShiftDto(
                            currentDate,
                            null,
                            "NONE",
                            "None",
                            null,
                            "NONE"
                    ));
                }
            }
            row.setShifts(dailyShifts);
            grid.add(row);
        }

        return grid;
    }
}
