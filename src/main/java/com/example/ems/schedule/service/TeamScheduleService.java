package com.example.ems.schedule.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.attendance.service.TeamResolutionService;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.entity.*;
import com.example.ems.schedule.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamScheduleService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MyShiftRepository shiftRepository;

    @Autowired
    private MyShiftTemplateRepository templateRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private MyScheduleChangeRequestRepository changeRequestRepository;

    @Autowired
    private TeamResolutionService teamResolutionService;

    @Autowired
    private ScheduleGridBuilder gridBuilder;

    @Autowired
    private KpiCalculator kpiCalculator;

    @Autowired
    private WorkforceTimeAnalyticsService workforceTimeAnalyticsService;

    @Autowired
    private SwapRequestService swapRequestService;

    @Autowired
    private MyScheduleTimelineEventRepository timelineRepository;

    public TeamScheduleResponse getTeamSchedule(LocalDate startDate, LocalDate endDate, Long departmentId,
            Long managerId, Integer page, Integer size, User currentUser) {
        if (startDate == null || endDate == null) {
            // Default to current week (Monday to Sunday)
            LocalDate today = LocalDate.now();
            startDate = today.minusDays(today.getDayOfWeek().getValue() - 1);
            endDate = startDate.plusDays(6);
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        String scope = "global";
        if (departmentId != null) {
            scope = "department";
        } else if (managerId != null) {
            scope = "manager";
        }

        List<Employee> allEmployees = teamResolutionService.resolveEmployees(scope, null, managerId, departmentId,
                currentUser);
        if (allEmployees.isEmpty()) {
            return new TeamScheduleResponse(
                    new TeamScheduleOverviewDto(0.0, 0, 0, 0, 90.0, 0.0, 0),
                    Collections.emptyList(),
                    Collections.emptyList(),
                    new OvertimeSummaryDto(0.0, Collections.emptyList()));
        }

        List<Long> allEmployeeIds = allEmployees.stream().map(Employee::getId).collect(Collectors.toList());

        // Paginate employees for the grid
        List<Employee> paginatedEmployees = allEmployees;
        if (page != null && size != null && size > 0) {
            int start = page * size;
            int end = Math.min(start + size, allEmployees.size());
            if (start < allEmployees.size()) {
                paginatedEmployees = allEmployees.subList(start, end);
            } else {
                paginatedEmployees = Collections.emptyList();
            }
        }

        List<Long> paginatedEmployeeIds = paginatedEmployees.stream().map(Employee::getId).collect(Collectors.toList());

        // Fetch shifts, leaves and swaps
        List<MyShift> allShifts = allEmployeeIds.isEmpty() ? Collections.emptyList()
                : shiftRepository.findByEmployeeIdInAndDateBetween(allEmployeeIds, startDate, endDate);

        List<Leave> allLeaves = allEmployeeIds.isEmpty() ? Collections.emptyList()
                : leaveRepository.findByEmployeeIdInAndStatus(allEmployeeIds, "APPROVED");

        List<MyScheduleChangeRequest> allSwaps = allEmployeeIds.isEmpty() ? Collections.emptyList()
                : changeRequestRepository.findByEmployeeIdIn(allEmployeeIds);

        // Grid builder uses paginated details
        List<MyShift> paginatedShifts = paginatedEmployeeIds.isEmpty() ? Collections.emptyList()
                : allShifts.stream().filter(s -> paginatedEmployeeIds.contains(s.getEmployee().getId()))
                        .collect(Collectors.toList());
        List<Leave> paginatedLeaves = paginatedEmployeeIds.isEmpty() ? Collections.emptyList()
                : allLeaves.stream().filter(l -> paginatedEmployeeIds.contains(l.getEmployee().getId()))
                        .collect(Collectors.toList());

        List<EmployeeScheduleGridDto> grid = gridBuilder.buildGrid(paginatedEmployees, paginatedShifts, paginatedLeaves,
                startDate, endDate);

        // Overtime monitor for paginated employees
        OvertimeSummaryDto overtime = workforceTimeAnalyticsService.calculateOvertimeAnalytics(paginatedEmployees,
                startDate, endDate);
        double totalOvertimeHours = overtime.getTotalOvertimeHours();

        // Calculate KPIs across all employees in scope
        TeamScheduleOverviewDto overview = kpiCalculator.calculateKpis(allEmployees, allShifts, allSwaps, startDate,
                endDate, totalOvertimeHours);

        // Get Swap requests DTO
        List<ShiftSwapRequestDto> swaps = swapRequestService.getSwapRequests(allSwaps);

        return new TeamScheduleResponse(overview, grid, swaps, overtime);
    }

    @Transactional
    public void assignShift(AssignShiftRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        ShiftType shiftType = request.getShiftType();
        if (shiftType == null) {
            throw new IllegalArgumentException("Shift type cannot be null");
        }

        Optional<MyShift> existingShift = shiftRepository.findByEmployeeEmailAndDate(employee.getEmail(),
                request.getDate());

        if (shiftType == ShiftType.NONE) {
            existingShift.ifPresent(shiftRepository::delete);
            // Log timeline event
            MyScheduleTimelineEvent event = new MyScheduleTimelineEvent();
            event.setEmployee(employee);
            event.setEvent("SHIFT_ASSIGNED");
            event.setPerformedBy("Manager");
            event.setPerformedAt(LocalDateTime.now());
            event.setDescription("Shift removed on " + request.getDate());
            timelineRepository.save(event);
            return;
        }

        // Map type to template ID
        Long templateId;
        String templateName;
        String startTime;
        String endTime;
        int breakMin;
        switch (shiftType) {
            case MORNING:
                templateId = 104L;
                templateName = "MORNING_SHIFT";
                startTime = "08:00";
                endTime = "14:00";
                breakMin = 30;
                break;
            case EVENING:
                templateId = 102L;
                templateName = "EVENING_SHIFT";
                startTime = "14:00";
                endTime = "22:00";
                breakMin = 45;
                break;
            case NIGHT:
                templateId = 103L;
                templateName = "NIGHT_SHIFT";
                startTime = "22:00";
                endTime = "06:00";
                breakMin = 45;
                break;
            case FULL_DAY:
            default:
                templateId = 101L;
                templateName = "GENERAL_SHIFT";
                startTime = "09:00";
                endTime = "18:00";
                breakMin = 60;
                break;
        }

        MyShiftTemplate template = templateRepository.findById(templateId).orElseGet(() -> {
            MyShiftTemplate t = new MyShiftTemplate(templateId, templateName, startTime, endTime, breakMin, "GLOBAL");
            return templateRepository.save(t);
        });

        MyShift shift = existingShift.orElseGet(() -> {
            MyShift s = new MyShift();
            s.setEmployee(employee);
            s.setDate(request.getDate());
            return s;
        });
        shift.setTemplate(template);
        shift.setStatus("ASSIGNED");
        shift.setUpdatedAt(LocalDateTime.now());
        shiftRepository.save(shift);

        // Log timeline event
        MyScheduleTimelineEvent event = new MyScheduleTimelineEvent();
        event.setEmployee(employee);
        event.setEvent("SHIFT_ASSIGNED");
        event.setPerformedBy("Manager");
        event.setPerformedAt(LocalDateTime.now());
        event.setDescription(template.getName() + " shift assigned on " + request.getDate());
        timelineRepository.save(event);
    }

    public void approveSwap(Long requestId) {
        swapRequestService.approveSwap(requestId);
    }

    public void rejectSwap(Long requestId) {
        swapRequestService.rejectSwap(requestId);
    }
}
