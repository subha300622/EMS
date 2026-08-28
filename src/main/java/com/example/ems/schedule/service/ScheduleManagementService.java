package com.example.ems.schedule.service;

import com.example.ems.auth.entity.User;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.schedule.dto.*;
import com.example.ems.schedule.entity.Schedule;
import com.example.ems.schedule.entity.ScheduleStatus;
import com.example.ems.schedule.repository.ScheduleRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// Service for managing schedule operations
@Service
public class ScheduleManagementService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private com.example.ems.schedule.repository.ScheduleExceptionRepository scheduleExceptionRepository;

    @Autowired
    private com.example.ems.holiday.repository.HolidayRepository holidayRepository;

    public EmployeeAvailabilityDto getEmployeeAvailability(User currentUser, String employeeIdInput, LocalDate date) {
        Long orgId = resolveOrganizationId(currentUser);
        Employee employee = resolveEmployeeInOrganization(employeeIdInput, orgId);
        String empCode = employee.getEmployeeId() != null ? employee.getEmployeeId() : employee.getId().toString();

        // 1. Priority Rule: Check Holiday FIRST
        if (holidayRepository != null && holidayRepository.existsByOrganizationIdAndHolidayDate(orgId, date)) {
            return new EmployeeAvailabilityDto(empCode, date, false, "HOLIDAY", null);
        }

        // 2. Check Active Leave Exception
        List<com.example.ems.schedule.entity.ScheduleException> exceptions = scheduleExceptionRepository.findActiveExceptionsOnDate(empCode, date);
        if (exceptions.isEmpty()) {
            exceptions = scheduleExceptionRepository.findActiveExceptionsOnDate(employee.getId().toString(), date);
        }

        if (!exceptions.isEmpty()) {
            com.example.ems.schedule.entity.ScheduleException exc = exceptions.get(0);
            return new EmployeeAvailabilityDto(empCode, date, false, "LEAVE", exc.getLeaveRequestId());
        }

        return new EmployeeAvailabilityDto(empCode, date, true, "NONE", null);
    }

    public Long resolveOrganizationId(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User context is required");
        }
        if (user.getOrganization() != null && user.getOrganization().getId() != null) {
            return user.getOrganization().getId();
        }
        if (user.getWorkEmail() != null) {
            Employee emp = employeeRepository.findByEmail(user.getWorkEmail()).orElse(null);
            if (emp != null && emp.getOrganization() != null && emp.getOrganization().getId() != null) {
                return emp.getOrganization().getId();
            }
        }
        // Fallback default organization ID if multi-tenant column is unset for superadmin/demo
        return 1L;
    }

    private LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Time value cannot be empty");
        }
        String clean = timeStr.trim();
        if (clean.length() == 5) {
            clean += ":00";
        }
        try {
            return LocalTime.parse(clean);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format: " + timeStr + ". Expected format HH:mm");
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Date value cannot be empty");
        }
        try {
            return LocalDate.parse(dateStr.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr + ". Expected format YYYY-MM-DD");
        }
    }

    private Employee resolveEmployeeInOrganization(String employeeIdInput, Long orgId) {
        Employee emp = null;
        if (employeeIdInput != null) {
            // Try numeric ID first
            try {
                Long numericId = Long.parseLong(employeeIdInput);
                emp = employeeRepository.findById(numericId).orElse(null);
            } catch (NumberFormatException ignored) {}

            // If not found by numeric ID, try code / employeeId string
            if (emp == null) {
                emp = employeeRepository.findByEmployeeId(employeeIdInput).orElse(null);
            }
        }

        if (emp == null) {
            throw new IllegalArgumentException("Employee not found with ID: " + employeeIdInput);
        }

        // Validate organization membership if employee organization is populated
        if (emp.getOrganization() != null && !orgId.equals(emp.getOrganization().getId())) {
            throw new IllegalArgumentException("Employee does not belong to the authenticated organization");
        }

        return emp;
    }

    private String resolveDepartmentName(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId).map(Department::getName).orElse(null);
    }

    private ScheduleDto mapToDto(Schedule s) {
        String empIdStr = s.getEmployee().getEmployeeId() != null 
                ? s.getEmployee().getEmployeeId() 
                : "EMP-" + s.getEmployee().getId();

        return new ScheduleDto(
                s.getScheduleId(),
                empIdStr,
                s.getEmployee().getFullName(),
                s.getDate().toString(),
                s.getStartTime().toString(),
                s.getEndTime().toString(),
                s.getStatus(),
                s.getLocation(),
                s.getNotes()
        );
    }

    public ScheduleListResponse getSchedules(
            User currentUser,
            String fromDateStr,
            String toDateStr,
            String employeeIdStr,
            Long teamId,
            Long departmentId,
            ScheduleStatus status,
            int page,
            int size) {

        Long orgId = resolveOrganizationId(currentUser);
        LocalDate fromDate = fromDateStr != null && !fromDateStr.isEmpty() ? parseDate(fromDateStr) : null;
        LocalDate toDate = toDateStr != null && !toDateStr.isEmpty() ? parseDate(toDateStr) : null;

        Long empNumericId = null;
        if (employeeIdStr != null && !employeeIdStr.isEmpty()) {
            try {
                empNumericId = Long.parseLong(employeeIdStr);
            } catch (NumberFormatException ignored) {}
        }

        String deptName = resolveDepartmentName(departmentId);

        final Long filterEmpNumericId = empNumericId;

        Specification<Schedule> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("organization").get("id"), orgId));

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), toDate));
            }
            if (filterEmpNumericId != null || (employeeIdStr != null && !employeeIdStr.isEmpty())) {
                Predicate p1 = filterEmpNumericId != null ? cb.equal(root.get("employee").get("id"), filterEmpNumericId) : null;
                Predicate p2 = (employeeIdStr != null && !employeeIdStr.isEmpty()) ? cb.equal(root.get("employee").get("employeeId"), employeeIdStr) : null;
                if (p1 != null && p2 != null) {
                    predicates.add(cb.or(p1, p2));
                } else if (p1 != null) {
                    predicates.add(p1);
                } else {
                    predicates.add(p2);
                }
            }
            if (teamId != null) {
                predicates.add(cb.equal(root.get("employee").get("team").get("id"), teamId));
            }
            if (deptName != null) {
                predicates.add(cb.equal(root.get("employee").get("department"), deptName));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending().and(Sort.by("startTime").ascending()));
        Page<Schedule> resultPage = scheduleRepository.findAll(spec, pageable);

        List<ScheduleDto> dtos = resultPage.getContent().stream().map(this::mapToDto).collect(Collectors.toList());
        return new ScheduleListResponse(dtos, resultPage.getTotalElements(), resultPage.getNumber(), resultPage.getSize());
    }

    public ScheduleDto getScheduleById(User currentUser, String scheduleId) {
        Long orgId = resolveOrganizationId(currentUser);
        Schedule schedule = scheduleRepository.findByScheduleIdAndOrganizationId(scheduleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with ID: " + scheduleId));
        return mapToDto(schedule);
    }

    @Transactional
    public ScheduleDto createSchedule(User currentUser, ScheduleCreateRequest req) {
        Long orgId = resolveOrganizationId(currentUser);
        Organization org = organizationRepository.findById(orgId)
                .orElseGet(() -> {
                    Organization o = new Organization();
                    o.setId(orgId);
                    o.setName("Default Organization");
                    o.setOrganizationCode("ORG-DEFAULT-" + orgId);
                    o.setNormalizedName("default organization " + orgId);
                    return organizationRepository.save(o);
                });

        Employee employee = resolveEmployeeInOrganization(req.getEmployeeId(), orgId);
        LocalDate date = parseDate(req.getDate());
        LocalTime startTime = parseTime(req.getStartTime());
        LocalTime endTime = parseTime(req.getEndTime());

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        if (holidayRepository != null && holidayRepository.existsByOrganizationIdAndHolidayDate(orgId, date)) {
            throw new IllegalStateException("EMPLOYEE_UNAVAILABLE_HOLIDAY: Target date " + date + " is an organization holiday");
        }

        String empCode = employee.getEmployeeId() != null ? employee.getEmployeeId() : employee.getId().toString();
        List<com.example.ems.schedule.entity.ScheduleException> leaveExc = scheduleExceptionRepository.findActiveExceptionsInDateRange(empCode, date, date);
        if (leaveExc.isEmpty()) {
            leaveExc = scheduleExceptionRepository.findActiveExceptionsInDateRange(employee.getId().toString(), date, date);
        }
        if (!leaveExc.isEmpty()) {
            throw new IllegalStateException("EMPLOYEE_ON_LEAVE: Employee is on approved leave for " + date);
        }

        boolean hasOverlap = scheduleRepository.existsOverlappingForCreate(orgId, employee.getId(), date, startTime, endTime);
        if (hasOverlap) {
            throw new IllegalStateException("Schedule overlaps with an existing schedule for employee on " + date);
        }

        long count = scheduleRepository.count();
        String generatedScheduleId = "SCH-" + String.format("%04d", count + 1);

        Schedule schedule = new Schedule();
        schedule.setScheduleId(generatedScheduleId);
        schedule.setEmployee(employee);
        schedule.setOrganization(org);
        schedule.setDate(date);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setStatus(ScheduleStatus.SCHEDULED);
        schedule.setLocation(req.getLocation());
        schedule.setNotes(req.getNotes());

        Schedule saved = scheduleRepository.save(schedule);
        return mapToDto(saved);
    }

    @Transactional
    public ScheduleDto updateSchedule(User currentUser, String scheduleId, ScheduleUpdateRequest req) {
        Long orgId = resolveOrganizationId(currentUser);
        Schedule schedule = scheduleRepository.findByScheduleIdAndOrganizationId(scheduleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with ID: " + scheduleId));

        LocalDate date = req.getDate() != null ? parseDate(req.getDate()) : schedule.getDate();
        LocalTime startTime = req.getStartTime() != null ? parseTime(req.getStartTime()) : schedule.getStartTime();
        LocalTime endTime = req.getEndTime() != null ? parseTime(req.getEndTime()) : schedule.getEndTime();

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        if (holidayRepository != null && holidayRepository.existsByOrganizationIdAndHolidayDate(orgId, date)) {
            throw new IllegalStateException("EMPLOYEE_UNAVAILABLE_HOLIDAY: Target date " + date + " is an organization holiday");
        }

        String empCode = schedule.getEmployee().getEmployeeId() != null ? schedule.getEmployee().getEmployeeId() : schedule.getEmployee().getId().toString();
        List<com.example.ems.schedule.entity.ScheduleException> leaveExc = scheduleExceptionRepository.findActiveExceptionsInDateRange(empCode, date, date);
        if (leaveExc.isEmpty()) {
            leaveExc = scheduleExceptionRepository.findActiveExceptionsInDateRange(schedule.getEmployee().getId().toString(), date, date);
        }
        if (!leaveExc.isEmpty()) {
            throw new IllegalStateException("EMPLOYEE_ON_LEAVE: Employee is on approved leave for " + date);
        }

        boolean hasOverlap = scheduleRepository.existsOverlappingForUpdate(orgId, schedule.getEmployee().getId(), date, schedule.getId(), startTime, endTime);
        if (hasOverlap) {
            throw new IllegalStateException("Schedule overlaps with an existing schedule for employee on " + date);
        }

        schedule.setDate(date);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        if (req.getStatus() != null) {
            schedule.setStatus(req.getStatus());
        }
        if (req.getLocation() != null) {
            schedule.setLocation(req.getLocation());
        }
        if (req.getNotes() != null) {
            schedule.setNotes(req.getNotes());
        }

        Schedule updated = scheduleRepository.save(schedule);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteSchedule(User currentUser, String scheduleId) {
        Long orgId = resolveOrganizationId(currentUser);
        Schedule schedule = scheduleRepository.findByScheduleIdAndOrganizationId(scheduleId, orgId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found with ID: " + scheduleId));
        scheduleRepository.delete(schedule);
    }

    public List<ScheduleDto> getSchedulesByEmployee(User currentUser, String employeeIdStr) {
        Long orgId = resolveOrganizationId(currentUser);
        Long empNumericId = null;
        try {
            empNumericId = Long.parseLong(employeeIdStr);
        } catch (NumberFormatException ignored) {}

        List<Schedule> list = scheduleRepository.findByEmployeeAndOrganization(orgId, empNumericId, employeeIdStr);
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ScheduleDto> getSchedulesByTeam(User currentUser, Long teamId) {
        Long orgId = resolveOrganizationId(currentUser);
        List<Schedule> list = scheduleRepository.findByTeamAndOrganization(orgId, teamId);
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<ScheduleDto> getSchedulesByDepartment(User currentUser, Long departmentId) {
        Long orgId = resolveOrganizationId(currentUser);
        String deptName = resolveDepartmentName(departmentId);
        List<Schedule> list = scheduleRepository.findByDepartmentNameAndOrganization(orgId, deptName);
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}
