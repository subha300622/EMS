package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.DepartmentCalendarSummaryDto;
import com.example.ems.attendance.dto.TeamCalendarGridDto;
import com.example.ems.attendance.dto.TeamHeatmapDto;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class TeamAttendanceCalendarServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private TeamAttendanceCalendarService calendarService;

    private Employee employee1;
    private Employee employee2;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        employee1 = new Employee();
        employee1.setId(1L);
        employee1.setFullName("Employee One");

        employee2 = new Employee();
        employee2.setId(2L);
        employee2.setFullName("Employee Two");
    }

    @Test
    public void testFebruaryLeapYear() {
        // Feb 2024 has 29 days
        LocalDate start = LocalDate.of(2024, 2, 1);
        LocalDate end = LocalDate.of(2024, 2, 29);

        when(attendanceRepository.findByEmployeeIdInAndDateBetween(any(), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());

        TeamCalendarGridDto grid = calendarService.getTeamMonthlyCalendar(
                List.of(employee1), start, end, null, null, "full");

        assertEquals(29, grid.getCalendar().size());
        assertEquals("2024-02-01", grid.getCalendar().get(0).getDate());
        assertEquals("2024-02-29", grid.getCalendar().get(28).getDate());
    }

    @Test
    public void testMonthWith31Days() {
        // July 2026 has 31 days
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end = LocalDate.of(2026, 7, 31);

        when(attendanceRepository.findByEmployeeIdInAndDateBetween(any(), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());

        TeamCalendarGridDto grid = calendarService.getTeamMonthlyCalendar(
                List.of(employee1), start, end, null, null, "full");

        assertEquals(31, grid.getCalendar().size());
    }

    @Test
    public void testEmptyDepartment() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);

        TeamCalendarGridDto grid = calendarService.getTeamMonthlyCalendar(
                Collections.emptyList(), start, end, 5L, null, "full");

        assertEquals(30, grid.getCalendar().size());
        for (TeamCalendarGridDto.CalendarDayDto day : grid.getCalendar()) {
            assertEquals(0, day.getSummary().getPresent());
            assertEquals(0, day.getSummary().getAbsent());
            assertTrue(day.getEmployees().isEmpty());
        }

        // Test heatmap with empty department (division-by-zero guard check)
        TeamHeatmapDto heatmap = calendarService.getTeamHeatmap(Collections.emptyList(), start, end);
        assertEquals(30, heatmap.getData().size());
        assertEquals(0, heatmap.getData().get(0).getPresentPercent());
        assertEquals("RED", heatmap.getData().get(0).getStatus());

        // Test summary KPI with empty department
        DepartmentCalendarSummaryDto summary = calendarService.getDepartmentCalendarSummary(
                Collections.emptyList(), start, end, 5L, null);
        assertEquals(0, summary.getTotals().getTotalEmployees());
        assertEquals(0.0, summary.getTotals().getAvgAttendance());
        assertNull(summary.getTrend().getBestDay());
        assertNull(summary.getTrend().getWorstDay());
    }

    @Test
    public void testZeroAttendanceScenario() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        LocalDate end = LocalDate.of(2026, 6, 30);

        when(attendanceRepository.findByEmployeeIdInAndDateBetween(any(), eq(start), eq(end)))
                .thenReturn(Collections.emptyList());

        // Heatmap should show RED status
        TeamHeatmapDto heatmap = calendarService.getTeamHeatmap(List.of(employee1, employee2), start, end);
        assertEquals(30, heatmap.getData().size());
        assertEquals(0, heatmap.getData().get(0).getPresentPercent());
        assertEquals("RED", heatmap.getData().get(0).getStatus());
    }

    @Test
    public void testSummaryKpiCalculations() {
        LocalDate start = LocalDate.of(2026, 6, 1); // Monday
        LocalDate end = LocalDate.of(2026, 6, 30);  // Tuesday
        // June 2026 has 22 weekdays

        // Seed 1 present record on 2026-06-01 (Monday) for employee1
        Attendance att1 = new Attendance();
        att1.setEmployee(employee1);
        att1.setDate(LocalDate.of(2026, 6, 1));
        att1.setStatus("PRESENT");

        // Seed 1 late record on 2026-06-02 (Tuesday) for employee1
        Attendance att2 = new Attendance();
        att2.setEmployee(employee1);
        att2.setDate(LocalDate.of(2026, 6, 2));
        att2.setStatus("LATE");

        when(attendanceRepository.findByEmployeeIdInAndDateBetween(any(), eq(start), eq(end)))
                .thenReturn(List.of(att1, att2));

        DepartmentCalendarSummaryDto summary = calendarService.getDepartmentCalendarSummary(
                List.of(employee1), start, end, 10L, null);

        assertEquals(22, summary.getTotals().getWorkingDays());
        assertEquals(1, summary.getTotals().getTotalEmployees());
        // Employee 1 is present on 2 out of 22 weekdays
        // Presence on June 1st = 100%, June 2nd = 100%, other 20 weekdays = 0%
        // Total weekdays average presence percent = (100.0 * 2) / 22 = 9.09% -> Math.round to 1 decimal place = 9.1%
        assertEquals(9.1, summary.getTotals().getAvgAttendance());
        assertEquals("2026-06-01", summary.getTrend().getBestDay());
        assertEquals("2026-06-03", summary.getTrend().getWorstDay()); // June 3rd has 0% attendance
    }
}
