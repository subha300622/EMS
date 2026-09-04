package com.example.ems.attendance.service;

import com.example.ems.attendance.dto.RegularizationProcessRequest;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.entity.AttendanceRegularization;
import com.example.ems.attendance.repository.AttendanceRegularizationRepository;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.auth.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AttendanceRegularizationService {

    @Autowired
    private AttendanceRegularizationRepository attendanceRegularizationRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.example.ems.auth.service.RoleService roleService;

    public List<AttendanceRegularization> getRegularizationsForUser(User currentUser, String status) {
        String email = currentUser.getWorkEmail();
        boolean hasAdminPerm = roleService.hasPermission(email, com.example.ems.auth.service.PermissionRegistry.ATTENDANCE_READ)
                || roleService.hasPermission(email, com.example.ems.auth.service.PermissionRegistry.ATTENDANCE_MANAGE);

        boolean hasSelfPerm = roleService.hasPermission(email, com.example.ems.auth.service.PermissionRegistry.ATTENDANCE_SELF_READ)
                || roleService.hasPermission(email, com.example.ems.auth.service.PermissionRegistry.EMPLOYEE_ATTENDANCE_READ);

        if (!hasAdminPerm && !hasSelfPerm) {
            throw new SecurityException("Access Denied: Requires 'attendance.read', 'attendance.manage', 'attendance.self.read', or 'employee.attendance.read' permission.");
        }

        if (hasAdminPerm) {
            if (status == null || status.isBlank()) {
                return attendanceRegularizationRepository.findAll();
            }
            return attendanceRegularizationRepository.findByStatus(status);
        } else {
            Employee employee = employeeRepository.findByEmail(email)
                    .orElseThrow(() -> new SecurityException("Employee profile not found for authenticated user."));
            if (status == null || status.isBlank()) {
                return attendanceRegularizationRepository.findByEmployeeId(employee.getId());
            }
            return attendanceRegularizationRepository.findByEmployeeIdAndStatus(employee.getId(), status);
        }
    }

    @Transactional
    public AttendanceRegularization submitRegularization(Long employeeId, LocalDate date, LocalTime proposedPunchIn, LocalTime proposedPunchOut, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        AttendanceRegularization regularization = new AttendanceRegularization();
        regularization.setEmployee(employee);
        regularization.setDate(date);
        regularization.setProposedPunchInTime(proposedPunchIn);
        regularization.setProposedPunchOutTime(proposedPunchOut);
        regularization.setReason(reason);
        regularization.setStatus("PENDING");

        return attendanceRegularizationRepository.save(regularization);
    }

    public List<AttendanceRegularization> getRegularizations(String status) {
        if (status == null || status.isBlank()) {
            return attendanceRegularizationRepository.findAll();
        }
        return attendanceRegularizationRepository.findByStatus(status);
    }

    @Transactional
    public AttendanceRegularization approveRegularization(Long id, RegularizationProcessRequest request) {
        AttendanceRegularization reg = attendanceRegularizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regularization not found with ID: " + id));
        if (!"PENDING".equalsIgnoreCase(reg.getStatus())) {
            throw new IllegalArgumentException("Regularization request is already processed: " + reg.getStatus());
        }

        reg.setStatus("APPROVED");
        if (request != null && request.getManagerNotes() != null) {
            reg.setManagerNotes(request.getManagerNotes());
        }
        attendanceRegularizationRepository.save(reg);

        Attendance attendance = attendanceRepository.findByEmployeeIdAndDate(reg.getEmployee().getId(), reg.getDate())
                .orElseGet(() -> {
                    Attendance newRecord = new Attendance();
                    newRecord.setEmployee(reg.getEmployee());
                    newRecord.setDate(reg.getDate());
                    return newRecord;
                });

        // Set original times only if not already set to preserve the very first original times
        if (attendance.getOriginalPunchInTime() == null) {
            attendance.setOriginalPunchInTime(attendance.getPunchInTime());
        }
        if (attendance.getOriginalPunchOutTime() == null) {
            attendance.setOriginalPunchOutTime(attendance.getPunchOutTime());
        }

        LocalTime approvedPunchIn = (request != null && request.getCorrectedPunchInTime() != null)
                ? request.getCorrectedPunchInTime()
                : reg.getProposedPunchInTime();

        LocalTime approvedPunchOut = (request != null && request.getCorrectedPunchOutTime() != null)
                ? request.getCorrectedPunchOutTime()
                : reg.getProposedPunchOutTime();

        attendance.setPunchInTime(approvedPunchIn);
        attendance.setPunchOutTime(approvedPunchOut);

        if (approvedPunchIn != null && approvedPunchIn.isAfter(LocalTime.of(9, 30))) {
            attendance.setStatus("LATE");
        } else {
            attendance.setStatus("PRESENT");
        }

        String notes = "Regularized: " + reg.getReason();
        if (request != null && request.getManagerNotes() != null && !request.getManagerNotes().isBlank()) {
            notes += " (Manager Notes: " + request.getManagerNotes() + ")";
        }
        attendance.setNotes(notes);

        attendanceRepository.save(attendance);

        return reg;
    }

    @Transactional
    public AttendanceRegularization rejectRegularization(Long id, RegularizationProcessRequest request) {
        AttendanceRegularization reg = attendanceRegularizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regularization not found with ID: " + id));
        if (!"PENDING".equalsIgnoreCase(reg.getStatus())) {
            throw new IllegalArgumentException("Regularization request is already processed: " + reg.getStatus());
        }

        reg.setStatus("REJECTED");
        if (request != null && request.getManagerNotes() != null) {
            reg.setManagerNotes(request.getManagerNotes());
        }
        return attendanceRegularizationRepository.save(reg);
    }
}
