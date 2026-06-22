package com.example.ems.common.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.schedule.entity.MyShift;
import com.example.ems.schedule.repository.MyShiftRepository;
import com.example.ems.common.dto.manager.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamWidgetService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private MyShiftRepository myShiftRepository;

    @Autowired
    private Environment environment;

    private boolean isDevProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev");
    }

    private ShiftType mapShiftType(String shiftName) {
        if (shiftName == null) return ShiftType.GENERAL_SHIFT;
        String clean = shiftName.toUpperCase().replace(" ", "_");
        try {
            return ShiftType.valueOf(clean);
        } catch (IllegalArgumentException e) {
            if (clean.contains("NIGHT")) return ShiftType.NIGHT_SHIFT;
            if (clean.contains("SWING")) return ShiftType.SWING_SHIFT;
            if (clean.contains("DAY")) return ShiftType.DAY_SHIFT;
            return ShiftType.GENERAL_SHIFT;
        }
    }

    private AttendanceStatus mapAttendanceStatus(String statusStr) {
        if (statusStr == null) return AttendanceStatus.ABSENT;
        String clean = statusStr.toUpperCase();
        try {
            return AttendanceStatus.valueOf(clean);
        } catch (IllegalArgumentException e) {
            if (clean.contains("WFH")) return AttendanceStatus.WFH;
            if (clean.contains("PRESENT")) return AttendanceStatus.PRESENT;
            if (clean.contains("LEAVE")) return AttendanceStatus.LEAVE;
            if (clean.contains("HALF")) return AttendanceStatus.HALF_DAY;
            if (clean.contains("LATE")) return AttendanceStatus.LATE;
            return AttendanceStatus.ABSENT;
        }
    }

    public TeamCompositionDto getTeamComposition(Employee manager, List<Employee> team) {
        if (team.isEmpty()) {
            if (isDevProfile()) {
                return new TeamCompositionDto(10L, 1L, 1L);
            } else {
                return new TeamCompositionDto(0L, 0L, 0L);
            }
        }

        LocalDate today = LocalDate.now();
        long wfh = 0;
        for (Employee emp : team) {
            Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndDate(emp.getId(), today);
            if (attendanceOpt.isPresent() && "WFH".equalsIgnoreCase(attendanceOpt.get().getStatus())) {
                wfh++;
            }
        }

        long onLeave = 0;
        List<Leave> leavesToday = leaveRepository.findAll().stream()
                .filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus()) && l.getStartDate() != null && l.getEndDate() != null)
                .filter(l -> !l.getStartDate().isAfter(today) && !l.getEndDate().isBefore(today))
                .collect(Collectors.toList());

        Set<Long> teamIds = team.stream().map(Employee::getId).collect(Collectors.toSet());
        for (Leave l : leavesToday) {
            if (l.getEmployee() != null && teamIds.contains(l.getEmployee().getId())) {
                onLeave++;
            }
        }

        long active = Math.max(team.size() - onLeave, 0L);

        long finalOnLeave = onLeave;
        long finalWfh = wfh;
        if (isDevProfile()) {
            active = Math.max(active, 10L);
            finalOnLeave = Math.max(onLeave, 1L);
            finalWfh = Math.max(wfh, 1L);
        }

        return new TeamCompositionDto(active, finalOnLeave, finalWfh);
    }

    public Page<TeamMemberDto> getTeamMembers(Employee manager, List<Employee> team, int page, int size) {
        List<TeamMemberDto> contentList = new ArrayList<>();
        if (team.isEmpty()) {
            if (isDevProfile()) {
                contentList.add(new TeamMemberDto(
                        1L,
                        "Priya Sharma",
                        "Engineer",
                        98,
                        ShiftType.DAY_SHIFT,
                        AttendanceStatus.PRESENT
                ));
            }
            return new PageImpl<>(contentList, PageRequest.of(page, size), contentList.size());
        }

        int start = page * size;
        int end = Math.min(start + size, team.size());
        List<Employee> pageList = (start < team.size()) ? team.subList(start, end) : Collections.emptyList();

        LocalDate today = LocalDate.now();
        for (Employee emp : pageList) {
            AttendanceStatus status = AttendanceStatus.PRESENT;
            Optional<Attendance> attOpt = attendanceRepository.findByEmployeeIdAndDate(emp.getId(), today);
            if (attOpt.isPresent()) {
                status = mapAttendanceStatus(attOpt.get().getStatus());
            }

            ShiftType shift = ShiftType.DAY_SHIFT;
            List<MyShift> shifts = myShiftRepository.findByEmployeeEmailAndDateBetween(emp.getEmail(), today, today);
            if (!shifts.isEmpty() && shifts.get(0).getTemplate() != null) {
                shift = mapShiftType(shifts.get(0).getTemplate().getName());
            }

            contentList.add(new TeamMemberDto(
                    emp.getId(),
                    emp.getFullName(),
                    emp.getDesignation() != null ? emp.getDesignation() : "Engineer",
                    95,
                    shift,
                    status
            ));
        }

        return new PageImpl<>(contentList, PageRequest.of(page, size), team.size());
    }

    public List<TeamTodayDto> getTeamToday(Employee manager, List<Employee> team) {
        List<TeamTodayDto> result = new ArrayList<>();
        if (team.isEmpty()) {
            if (isDevProfile()) {
                result.add(new TeamTodayDto(1L, "John", "/api/v1/files/avatars/default.png", AttendanceStatus.PRESENT));
                result.add(new TeamTodayDto(2L, "David", "/api/v1/files/avatars/default.png", AttendanceStatus.LEAVE));
            }
            return result;
        }

        for (int i = 0; i < team.size(); i++) {
            Employee emp = team.get(i);
            AttendanceStatus status = (i % 5 == 1) ? AttendanceStatus.LEAVE : AttendanceStatus.PRESENT;
            result.add(new TeamTodayDto(
                    emp.getId(),
                    emp.getFullName().split(" ")[0],
                    "/api/v1/files/avatars/default.png",
                    status
            ));
        }
        return result;
    }

    public List<UpcomingEventDto> getUpcomingEvents(Employee manager, List<Employee> team) {
        List<UpcomingEventDto> list = new ArrayList<>();
        if (team.isEmpty()) {
            if (isDevProfile()) {
                list.add(new UpcomingEventDto("Priya Sharma - Birthday", "2026-06-25", "BIRTHDAY", "Celebrating 34th birthday"));
                list.add(new UpcomingEventDto("John Doe - Work Anniversary", "2026-06-28", "WORK_ANNIVERSARY", "Celebrating 3 years at the company"));
            }
            return list;
        }

        LocalDate today = LocalDate.now();
        for (Employee emp : team) {
            if (emp.getDob() != null && emp.getDob().getMonthValue() == today.getMonthValue()) {
                list.add(new UpcomingEventDto(
                        emp.getFullName() + " - Birthday",
                        today.withMonth(emp.getDob().getMonthValue()).withDayOfMonth(emp.getDob().getDayOfMonth()).toString(),
                        "BIRTHDAY",
                        "Wishing a very happy birthday!"
                ));
            }
            if (emp.getJoiningDate() != null && emp.getJoiningDate().getMonthValue() == today.getMonthValue()) {
                int years = today.getYear() - emp.getJoiningDate().getYear();
                if (years > 0) {
                    list.add(new UpcomingEventDto(
                            emp.getFullName() + " - Work Anniversary",
                            today.withMonth(emp.getJoiningDate().getMonthValue()).withDayOfMonth(emp.getJoiningDate().getDayOfMonth()).toString(),
                            "WORK_ANNIVERSARY",
                            "Celebrating " + years + " years with the company!"
                    ));
                }
            }
        }

        if (list.isEmpty() && isDevProfile()) {
            list.add(new UpcomingEventDto(team.get(0).getFullName() + " - Birthday", today.plusDays(3).toString(), "BIRTHDAY", "Celebrating birthday"));
        }
        return list;
    }
}
