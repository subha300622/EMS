package com.example.ems.schedule.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveTypeRepository;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.entity.*;
import com.example.ems.schedule.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MyScheduleService {

    @Autowired
    private MyShiftTemplateRepository templateRepository;

    @Autowired
    private MyShiftRepository shiftRepository;

    @Autowired
    private MyScheduleMeetingRepository meetingRepository;

    @Autowired
    private MyScheduleChangeRequestRepository changeRequestRepository;

    @Autowired
    private MyEmployeeAvailabilityRepository availabilityRepository;

    @Autowired
    private MyScheduleTimelineEventRepository timelineRepository;

    @Autowired
    private MyScheduleNotificationRepository notificationRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Transactional
    public void seedScheduleData(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        if (employee == null) return;

        // 1. Seed Shift Templates (Manually set IDs to match change-requests requests)
        MyShiftTemplate general = templateRepository.findById(101L).orElseGet(() -> {
            MyShiftTemplate t = new MyShiftTemplate(101L, "GENERAL_SHIFT", "09:00", "18:00", 60, "GLOBAL");
            return templateRepository.save(t);
        });

        MyShiftTemplate evening = templateRepository.findById(102L).orElseGet(() -> {
            MyShiftTemplate t = new MyShiftTemplate(102L, "EVENING_SHIFT", "14:00", "22:00", 45, "GLOBAL");
            return templateRepository.save(t);
        });

        templateRepository.findById(103L).orElseGet(() -> {
            MyShiftTemplate t = new MyShiftTemplate(103L, "NIGHT_SHIFT", "22:00", "06:00", 45, "GLOBAL");
            return templateRepository.save(t);
        });

        // 2. Seed assigned shifts for June 2026
        if (shiftRepository.findByEmployeeEmail(email).isEmpty()) {
            LocalDate start = LocalDate.of(2026, 6, 1);
            LocalDate end = LocalDate.of(2026, 6, 30);
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                // Skip weekends for general shift seeding
                int dayVal = date.getDayOfWeek().getValue();
                if (dayVal == 6 || dayVal == 7) continue;

                MyShift shift = new MyShift();
                shift.setEmployee(employee);
                shift.setDate(date);
                shift.setTemplate(date.isEqual(LocalDate.of(2026, 6, 16)) ? general : (date.getDayOfMonth() % 3 == 0 ? evening : general));
                shift.setStatus(date.isBefore(LocalDate.of(2026, 6, 16)) ? "COMPLETED" : "ASSIGNED");
                shiftRepository.save(shift);
            }
        }

        // 3. Seed Meetings today (June 16th, 2026) and upcoming
        if (meetingRepository.findByEmployeeEmail(email).isEmpty()) {
            LocalDate today = LocalDate.of(2026, 6, 16);
            
            // Today meetings
            MyScheduleMeeting m1 = new MyScheduleMeeting();
            m1.setTitle("Daily Stand-up");
            m1.setStartDateTime(LocalDateTime.of(today, LocalTime.of(10, 0)));
            m1.setEndDateTime(LocalDateTime.of(today, LocalTime.of(10, 15)));
            m1.setLocation("Conference Room B");
            m1.setStatus("CONFIRMED");
            m1.setEmployee(employee);
            meetingRepository.save(m1);

            MyScheduleMeeting m2 = new MyScheduleMeeting();
            m2.setTitle("Sprint Planning Meeting");
            m2.setStartDateTime(LocalDateTime.of(today, LocalTime.of(11, 0)));
            m2.setEndDateTime(LocalDateTime.of(today, LocalTime.of(12, 0)));
            m2.setLocation("Conference Room A");
            m2.setStatus("CONFIRMED");
            m2.setEmployee(employee);
            meetingRepository.save(m2);

            MyScheduleMeeting m3 = new MyScheduleMeeting();
            m3.setTitle("Technical Design Sync");
            m3.setStartDateTime(LocalDateTime.of(today, LocalTime.of(15, 0)));
            m3.setEndDateTime(LocalDateTime.of(today, LocalTime.of(16, 0)));
            m3.setLocation("Virtual (MS Teams)");
            m3.setStatus("CONFIRMED");
            m3.setEmployee(employee);
            meetingRepository.save(m3);

            // Upcoming meetings
            MyScheduleMeeting m4 = new MyScheduleMeeting();
            m4.setTitle("Daily Stand-up");
            m4.setStartDateTime(LocalDateTime.of(today.plusDays(1), LocalTime.of(10, 0)));
            m4.setEndDateTime(LocalDateTime.of(today.plusDays(1), LocalTime.of(10, 15)));
            m4.setLocation("Conference Room B");
            m4.setStatus("CONFIRMED");
            m4.setEmployee(employee);
            meetingRepository.save(m4);

            MyScheduleMeeting m5 = new MyScheduleMeeting();
            m5.setTitle("Client Discussion");
            m5.setStartDateTime(LocalDateTime.of(today.plusDays(4), LocalTime.of(15, 0)));
            m5.setEndDateTime(LocalDateTime.of(today.plusDays(4), LocalTime.of(16, 0)));
            m5.setLocation("Boardroom A");
            m5.setStatus("SCHEDULED");
            m5.setEmployee(employee);
            meetingRepository.save(m5);
        }

        // 4. Seed Leave record
        if (leaveRepository.findByEmployeeId(employee.getId()).isEmpty()) {
            LeaveType annualLeave = leaveTypeRepository.findAll().stream()
                    .filter(t -> "Annual Leave".equalsIgnoreCase(t.getName()) || "Privilege Leave".equalsIgnoreCase(t.getName()))
                    .findFirst()
                    .orElseGet(() -> {
                        LeaveType lt = new LeaveType();
                        lt.setName("Annual Leave");
                        lt.setDefaultDays(21);
                        return leaveTypeRepository.save(lt);
                    });

            Leave leave = new Leave();
            leave.setEmployee(employee);
            leave.setLeaveType(annualLeave);
            leave.setStartDate(LocalDate.of(2026, 6, 25));
            leave.setEndDate(LocalDate.of(2026, 6, 27));
            leave.setReason("Annual family vacation");
            leave.setStatus("APPROVED");
            leaveRepository.save(leave);
        }

        // 5. Seed Attendance check-in today (June 16th, 2026)
        if (attendanceRepository.findByEmployeeIdAndDate(employee.getId(), LocalDate.of(2026, 6, 16)).isEmpty()) {
            Attendance att = new Attendance();
            att.setEmployee(employee);
            att.setDate(LocalDate.of(2026, 6, 16));
            att.setStatus("PRESENT");
            att.setPunchInTime(LocalTime.of(9, 0));
            attendanceRepository.save(att);
        }

        // 6. Seed default Availability slots
        if (availabilityRepository.findByEmployeeEmail(email).isEmpty()) {
            String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"};
            for (String day : days) {
                MyEmployeeAvailability av = new MyEmployeeAvailability();
                av.setEmployee(employee);
                av.setDayOfWeek(day);
                av.setAvailableFrom("MONDAY".equals(day) ? "09:00" : ("FRIDAY".equals(day) ? "10:00" : "09:00"));
                av.setAvailableTo("MONDAY".equals(day) ? "18:00" : ("FRIDAY".equals(day) ? "16:00" : "18:00"));
                availabilityRepository.save(av);
            }
        }

        // 7. Seed Timeline events
        if (timelineRepository.findByEmployeeEmailOrderByPerformedAtDesc(email).isEmpty()) {
            MyScheduleTimelineEvent e1 = new MyScheduleTimelineEvent();
            e1.setEmployee(employee);
            e1.setEvent("SHIFT_ASSIGNED");
            e1.setPerformedBy("HR Manager");
            e1.setPerformedAt(LocalDateTime.of(2026, 6, 1, 9, 0));
            e1.setDescription("General shift assigned");
            timelineRepository.save(e1);

            MyScheduleTimelineEvent e2 = new MyScheduleTimelineEvent();
            e2.setEmployee(employee);
            e2.setEvent("SHIFT_CHANGE_REQUESTED");
            e2.setPerformedBy(employee.getFullName());
            e2.setPerformedAt(LocalDateTime.of(2026, 6, 16, 11, 0));
            e2.setDescription("Requested evening shift");
            timelineRepository.save(e2);
        }

        // 8. Seed Notifications
        if (notificationRepository.findByEmployeeEmailOrderByCreatedAtDesc(email).isEmpty()) {
            MyScheduleNotification n = new MyScheduleNotification();
            n.setEmployee(employee);
            n.setType("MEETING_REMINDER");
            n.setMessage("Sprint Planning meeting starts in 30 minutes");
            n.setCreatedAt(LocalDateTime.of(2026, 6, 17, 9, 30));
            n.setIsRead(false);
            notificationRepository.save(n);
        }

        // 9. Seed Schedule Change Request
        if (changeRequestRepository.findByEmployeeEmail(email).isEmpty()) {
            MyScheduleChangeRequest scr = new MyScheduleChangeRequest();
            scr.setEmployee(employee);
            scr.setRequestNumber("SCR-2026-0001-" + employee.getId());
            scr.setCurrentShift(general);
            scr.setRequestedShift(evening);
            scr.setRequestedDate(LocalDate.of(2026, 6, 20));
            scr.setRequestType("SHIFT_CHANGE");
            scr.setReason("Personal appointment");
            scr.setStatus("PENDING_MANAGER_APPROVAL");
            scr.setSubmittedAt(LocalDateTime.of(2026, 6, 16, 11, 0));
            changeRequestRepository.save(scr);
        }
    }

    public MyScheduleDashboardResponse getDashboard(String email) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow();
        LocalDate today = LocalDate.of(2026, 6, 16);

        MyScheduleDashboardResponse res = new MyScheduleDashboardResponse();

        // 1. Employee Info
        MyScheduleDashboardResponse.EmployeeInfo empInfo = new MyScheduleDashboardResponse.EmployeeInfo();
        empInfo.setEmployeeId(employee.getId());
        empInfo.setEmployeeCode(employee.getEmployeeId());
        empInfo.setFullName(employee.getFullName());
        empInfo.setDesignation(employee.getDesignation());
        empInfo.setDepartment(employee.getDepartment());
        empInfo.setManagerName(employee.getManager() != null ? employee.getManager().getFullName() : "Alex Smith");
        res.setEmployee(empInfo);

        // 2. Today's Summary
        MyScheduleDashboardResponse.TodaySummary todaySum = new MyScheduleDashboardResponse.TodaySummary();
        todaySum.setDate(today.toString());
        
        Optional<MyShift> shiftOpt = shiftRepository.findByEmployeeEmailAndDate(email, today);
        if (shiftOpt.isPresent()) {
            MyShiftTemplate temp = shiftOpt.get().getTemplate();
            todaySum.setShift(temp.getName());
            todaySum.setWorkingHours(temp.getStartTime() + " - " + temp.getEndTime());
        } else {
            todaySum.setShift("GENERAL_SHIFT");
            todaySum.setWorkingHours("09:00 - 18:00");
        }

        Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndDate(employee.getId(), today);
        todaySum.setAttendanceStatus(attendanceOpt.isPresent() ? "CHECKED_IN" : "ABSENT");

        // Count meetings today
        LocalDateTime dayStart = LocalDateTime.of(today, LocalTime.MIN);
        LocalDateTime dayEnd = LocalDateTime.of(today, LocalTime.MAX);
        long meetingsTodayCount = meetingRepository.findByEmployeeEmailAndStartDateTimeBetween(email, dayStart, dayEnd).size();
        todaySum.setMeetingsToday((int) meetingsTodayCount);
        todaySum.setTasksToday(5); // Static 5 tasks as requested
        res.setTodaySummary(todaySum);

        // 3. Upcoming Summary
        MyScheduleDashboardResponse.UpcomingSummary upSum = new MyScheduleDashboardResponse.UpcomingSummary();
        long upcomingMeetingsCount = meetingRepository.findByEmployeeEmailAndStartDateTimeAfter(email, dayEnd).size();
        upSum.setUpcomingMeetings((int) upcomingMeetingsCount);

        List<Leave> leaves = leaveRepository.findByEmployeeId(employee.getId());
        long upcomingLeavesCount = leaves.stream().filter(l -> l.getStartDate().isAfter(today)).count();
        upSum.setUpcomingLeaves((int) upcomingLeavesCount);

        Page<MyScheduleChangeRequest> p = changeRequestRepository.findByFilters(email, "PENDING_MANAGER_APPROVAL", Pageable.ofSize(10));
        upSum.setPendingChangeRequests((int) p.getTotalElements());
        res.setUpcomingSummary(upSum);

        res.setLastUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        return res;
    }

    public MyCalendarResponse getCalendar(String email, String view, String startDateStr, String endDateStr, String eventType) {
        LocalDate start = LocalDate.parse(startDateStr);
        LocalDate end = LocalDate.parse(endDateStr);

        List<MyCalendarResponse.CalendarEvent> eventList = new ArrayList<>();

        // 1. Shifts
        if (eventType == null || "SHIFT".equalsIgnoreCase(eventType)) {
            List<MyShift> shifts = shiftRepository.findByEmployeeEmailAndDateBetween(email, start, end);
            for (MyShift s : shifts) {
                MyCalendarResponse.CalendarEvent ev = new MyCalendarResponse.CalendarEvent();
                ev.setEventId(s.getId() + 1000);
                ev.setEventType("SHIFT");
                ev.setTitle(s.getTemplate().getName());
                ev.setStartDate(s.getDate().toString());
                ev.setEndDate(s.getDate().toString());
                ev.setLocation(s.getEmployee().getLocation() != null ? s.getEmployee().getLocation() : "Headquarters");
                ev.setStatus(s.getStatus());
                eventList.add(ev);
            }
        }

        // 2. Leaves
        if (eventType == null || "LEAVE".equalsIgnoreCase(eventType)) {
            Employee employee = employeeRepository.findByEmail(email).orElseThrow();
            List<Leave> leaves = leaveRepository.findByEmployeeId(employee.getId());
            for (Leave l : leaves) {
                if (!l.getStartDate().isAfter(end) && !l.getEndDate().isBefore(start)) {
                    MyCalendarResponse.CalendarEvent ev = new MyCalendarResponse.CalendarEvent();
                    ev.setEventId(l.getId() + 2000);
                    ev.setEventType("LEAVE");
                    ev.setTitle(l.getLeaveType().getName());
                    ev.setStartDate(l.getStartDate().toString());
                    ev.setEndDate(l.getEndDate().toString());
                    ev.setStatus(l.getStatus());
                    eventList.add(ev);
                }
            }
        }

        // 3. Meetings
        if (eventType == null || "MEETING".equalsIgnoreCase(eventType)) {
            List<MyScheduleMeeting> meetings = meetingRepository.findByEmployeeEmailAndStartDateTimeBetween(
                    email, LocalDateTime.of(start, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
            for (MyScheduleMeeting m : meetings) {
                MyCalendarResponse.CalendarEvent ev = new MyCalendarResponse.CalendarEvent();
                ev.setEventId(m.getId() + 3000);
                ev.setEventType("MEETING");
                ev.setTitle(m.getTitle());
                ev.setStartDateTime(m.getStartDateTime().format(DateTimeFormatter.ISO_DATE_TIME) + "Z");
                ev.setEndDateTime(m.getEndDateTime().format(DateTimeFormatter.ISO_DATE_TIME) + "Z");
                ev.setLocation(m.getLocation());
                ev.setStatus(m.getStatus());
                eventList.add(ev);
            }
        }

        // 4. Holidays
        if (eventType == null || "HOLIDAY".equalsIgnoreCase(eventType)) {
            // Seed a mock holiday on June 15th
            LocalDate holDate = LocalDate.of(2026, 6, 15);
            if (!holDate.isBefore(start) && !holDate.isAfter(end)) {
                MyCalendarResponse.CalendarEvent ev = new MyCalendarResponse.CalendarEvent();
                ev.setEventId(9999L);
                ev.setEventType("HOLIDAY");
                ev.setTitle("Mid-Year Holiday");
                ev.setStartDate(holDate.toString());
                ev.setEndDate(holDate.toString());
                ev.setStatus("ACTIVE");
                eventList.add(ev);
            }
        }

        return new MyCalendarResponse(eventList);
    }

    public TodayScheduleResponse getTodaySchedule(String email) {
        LocalDate today = LocalDate.of(2026, 6, 16);

        TodayScheduleResponse res = new TodayScheduleResponse();
        res.setDate(today.toString());

        Optional<MyShift> shiftOpt = shiftRepository.findByEmployeeEmailAndDate(email, today);
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        TodayScheduleResponse.ShiftInfo sInfo = new TodayScheduleResponse.ShiftInfo();
        if (shiftOpt.isPresent()) {
            MyShiftTemplate t = shiftOpt.get().getTemplate();
            sInfo.setShiftId(t.getId());
            sInfo.setName(t.getName());
            sInfo.setStartTime(t.getStartTime());
            sInfo.setEndTime(t.getEndTime());
            sInfo.setBreakDurationMinutes(t.getBreakDurationMinutes());
            sInfo.setLocation(employee != null && employee.getLocation() != null ? employee.getLocation() : "Headquarters");
            res.setWorkingStatus("WORKING");
        } else {
            sInfo.setShiftId(101L);
            sInfo.setName("GENERAL_SHIFT");
            sInfo.setStartTime("09:00");
            sInfo.setEndTime("18:00");
            sInfo.setBreakDurationMinutes(60);
            sInfo.setLocation(employee != null && employee.getLocation() != null ? employee.getLocation() : "Headquarters");
            res.setWorkingStatus("WORKING");
        }
        res.setShift(sInfo);

        // Load meetings for today
        List<MyScheduleMeeting> meetings = meetingRepository.findByEmployeeEmailAndStartDateTimeBetween(
                email, LocalDateTime.of(today, LocalTime.MIN), LocalDateTime.of(today, LocalTime.MAX));
        res.setEvents(meetings.stream().map(m -> new TodayScheduleResponse.TodayEvent(
                m.getId() + 5000L,
                m.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                m.getTitle(),
                "MEETING"
        )).collect(Collectors.toList()));

        return res;
    }

    public UpcomingScheduleResponse getUpcomingSchedule(String email, Integer days, String eventType) {
        LocalDate today = LocalDate.of(2026, 6, 16);
        int limitDays = days != null ? days : 7;
        LocalDate limitDate = today.plusDays(limitDays);

        List<UpcomingScheduleResponse.UpcomingEvent> eventList = new ArrayList<>();

        if (eventType == null || "MEETING".equalsIgnoreCase(eventType)) {
            List<MyScheduleMeeting> meetings = meetingRepository.findByEmployeeEmailAndStartDateTimeBetween(
                    email, LocalDateTime.of(today, LocalTime.MIN), LocalDateTime.of(limitDate, LocalTime.MAX));
            for (MyScheduleMeeting m : meetings) {
                eventList.add(new UpcomingScheduleResponse.UpcomingEvent(
                        m.getId() + 3000L,
                        "MEETING",
                        m.getTitle(),
                        m.getStartDateTime().format(DateTimeFormatter.ISO_DATE_TIME) + "Z",
                        m.getStatus()
                ));
            }
        }

        return new UpcomingScheduleResponse(eventList);
    }

    public ShiftHistoryResponse getMyShiftHistory(String email, String month, String status) {
        List<MyShift> shifts = shiftRepository.findByFilters(email, month, status);
        return new ShiftHistoryResponse(shifts.stream().map(s -> {
            ShiftHistoryResponse.ShiftItem item = new ShiftHistoryResponse.ShiftItem();
            item.setShiftId(s.getTemplate().getId());
            item.setShiftName(s.getTemplate().getName());
            item.setDate(s.getDate().toString());
            item.setStartTime(s.getTemplate().getStartTime());
            item.setEndTime(s.getTemplate().getEndTime());
            item.setDurationHours(8); // Standard 8 hours
            item.setLocation(s.getEmployee().getLocation() != null ? s.getEmployee().getLocation() : "Headquarters");
            item.setStatus(s.getStatus());
            return item;
        }).collect(Collectors.toList()));
    }

    @Transactional
    public ChangeRequestResponse createChangeRequest(String email, ChangeRequestPayload req) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow();
        MyShiftTemplate current = templateRepository.findById(req.getCurrentShiftId()).orElseThrow();
        MyShiftTemplate requested = templateRepository.findById(req.getRequestedShiftId()).orElseThrow();

        MyScheduleChangeRequest scr = new MyScheduleChangeRequest();
        scr.setEmployee(employee);
        
        long count = changeRequestRepository.count();
        scr.setRequestNumber("SCR-2026-" + String.format("%04d", count + 1));
        scr.setCurrentShift(current);
        scr.setRequestedShift(requested);
        scr.setRequestedDate(LocalDate.parse(req.getRequestedDate()));
        scr.setRequestType(req.getRequestType() != null ? req.getRequestType() : "SHIFT_CHANGE");
        scr.setReason(req.getReason());
        scr.setStatus("PENDING_MANAGER_APPROVAL");
        scr.setSubmittedAt(LocalDateTime.now());
        
        MyScheduleChangeRequest saved = changeRequestRepository.save(scr);

        // Timeline log
        MyScheduleTimelineEvent event = new MyScheduleTimelineEvent();
        event.setEmployee(employee);
        event.setEvent("SHIFT_CHANGE_REQUESTED");
        event.setPerformedBy(employee.getFullName());
        event.setPerformedAt(LocalDateTime.now());
        event.setDescription("Requested shift change to " + requested.getName() + " on " + req.getRequestedDate());
        timelineRepository.save(event);

        return new ChangeRequestResponse(
                saved.getId(),
                saved.getRequestNumber(),
                saved.getStatus(),
                saved.getSubmittedAt().format(DateTimeFormatter.ISO_DATE_TIME) + "Z",
                "Schedule change request submitted successfully"
        );
    }

    public ChangeRequestListResponse getChangeRequests(String email, String status, Pageable pageable) {
        Page<MyScheduleChangeRequest> page = changeRequestRepository.findByFilters(email, status, pageable);
        List<ChangeRequestListResponse.ChangeRequestItem> items = page.getContent().stream().map(r -> {
            ChangeRequestListResponse.ChangeRequestItem item = new ChangeRequestListResponse.ChangeRequestItem();
            item.setRequestId(r.getId());
            item.setRequestNumber(r.getRequestNumber());
            item.setCurrentShift(r.getCurrentShift().getName());
            item.setRequestedShift(r.getRequestedShift().getName());
            item.setRequestedDate(r.getRequestedDate().toString());
            item.setStatus(r.getStatus());
            item.setRequestedAt(r.getSubmittedAt().format(DateTimeFormatter.ISO_DATE_TIME) + "Z");
            return item;
        }).collect(Collectors.toList());

        ChangeRequestListResponse res = new ChangeRequestListResponse();
        res.setContent(items);
        res.setPagination(new ChangeRequestListResponse.PaginationInfo(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        ));
        return res;
    }

    @Transactional
    public AvailabilityResponse updateAvailability(String email, AvailabilityRequest req) {
        Employee employee = employeeRepository.findByEmail(email).orElseThrow();
        availabilityRepository.deleteByEmployeeEmail(email);

        for (AvailabilityRequest.AvailabilitySlot slot : req.getAvailability()) {
            MyEmployeeAvailability av = new MyEmployeeAvailability();
            av.setEmployee(employee);
            av.setDayOfWeek(slot.getDayOfWeek());
            av.setAvailableFrom(slot.getAvailableFrom());
            av.setAvailableTo(slot.getAvailableTo());
            av.setUpdatedAt(LocalDateTime.now());
            availabilityRepository.save(av);
        }

        // Timeline log
        MyScheduleTimelineEvent event = new MyScheduleTimelineEvent();
        event.setEmployee(employee);
        event.setEvent("AVAILABILITY_UPDATED");
        event.setPerformedBy(employee.getFullName());
        event.setPerformedAt(LocalDateTime.now());
        event.setDescription("Updated preferred working availability settings");
        timelineRepository.save(event);

        return new AvailabilityResponse(
                "UPDATED",
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "Z",
                "Availability updated successfully"
        );
    }

    public ScheduleTimelineResponse getTimeline(String email) {
        List<MyScheduleTimelineEvent> events = timelineRepository.findByEmployeeEmailOrderByPerformedAtDesc(email);
        return new ScheduleTimelineResponse(events.stream().map(e -> new ScheduleTimelineResponse.TimelineActivity(
                e.getEvent(),
                e.getPerformedBy(),
                e.getPerformedAt().format(DateTimeFormatter.ISO_DATE_TIME) + "Z",
                e.getDescription()
        )).collect(Collectors.toList()));
    }

    public ScheduleNotificationsResponse getNotifications(String email) {
        List<MyScheduleNotification> notifications = notificationRepository.findByEmployeeEmailOrderByCreatedAtDesc(email);
        return new ScheduleNotificationsResponse(notifications.stream().map(n -> new ScheduleNotificationsResponse.NotificationItem(
                n.getId(),
                n.getType(),
                n.getMessage(),
                n.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME) + "Z",
                n.getIsRead()
        )).collect(Collectors.toList()));
    }

    public SchedulePoliciesResponse getPolicies() {
        SchedulePoliciesResponse.PolicyInfo info = new SchedulePoliciesResponse.PolicyInfo(
                5,
                "09:00-18:00",
                true,
                24,
                true
        );
        return new SchedulePoliciesResponse(info);
    }
}
