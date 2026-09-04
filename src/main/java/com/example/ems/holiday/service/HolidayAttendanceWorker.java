package com.example.ems.holiday.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.holiday.entity.Holiday;
import com.example.ems.holiday.entity.HolidayStatus;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class HolidayAttendanceWorker {

    private static final Logger log = LoggerFactory.getLogger(HolidayAttendanceWorker.class);

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * Daily scheduled job running at midnight (00:05 AM) to process holiday attendance.
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void runDailyHolidayJob() {
        processDailyHolidays(LocalDate.now());
    }

    /**
     * Idempotent business method to process organization-wide holidays for a given date.
     */
    @Transactional
    public void processDailyHolidays(LocalDate targetDate) {
        List<Organization> orgs = organizationRepository.findAll();

        for (Organization org : orgs) {
            holidayRepository.findByOrganizationIdAndHolidayDateAndStatus(org.getId(), targetDate, HolidayStatus.ACTIVE)
                    .ifPresent(holiday -> markOrganizationHolidayAttendance(org.getId(), holiday, targetDate));
        }
    }

    @Transactional
    public void markOrganizationHolidayAttendance(Long orgId, Holiday holiday, LocalDate targetDate) {
        // Query ACTIVE employees belonging to the organization
        List<Employee> activeEmployees = employeeRepository.findByOrganizationIdAndStatus(orgId, "ACTIVE");

        for (Employee emp : activeEmployees) {
            // Precedence rule: Keep existing finalized attendance record
            boolean exists = attendanceRepository.existsByEmployeeIdAndDate(emp.getId(), targetDate);
            if (!exists) {
                Attendance attendance = new Attendance();
                attendance.setEmployee(emp);
                attendance.setDate(targetDate);
                attendance.setStatus("HOLIDAY");
                attendance.setAttendanceType("HOLIDAY");
                attendance.setNotes("Organization Holiday: " + holiday.getName());
                attendance.setServerTime(java.time.Instant.now());

                attendanceRepository.save(attendance);
                log.info("Marked HOLIDAY attendance for employee {} on {}", emp.getEmployeeId(), targetDate);
            }
        }
    }
}
