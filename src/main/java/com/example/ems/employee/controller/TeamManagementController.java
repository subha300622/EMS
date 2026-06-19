package com.example.ems.employee.controller;

import com.example.ems.asset.service.MyAssetService;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.service.PerformanceService;
import com.example.ems.schedule.service.MyScheduleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.service.TrainingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/team")
@CrossOrigin("*")
@Tag(name = "Directory")
public class TeamManagementController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private MyScheduleService myScheduleService;

    @Autowired
    private PerformanceService performanceService;

    @Autowired
    private TrainingService trainingService;

    @Autowired
    private MyAssetService myAssetService;

    @GetMapping
    public ResponseEntity<?> getTeamDirectory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity.ok(ApiResponse.success("Team directory retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        return ResponseEntity.ok(ApiResponse.success("Team directory retrieved successfully", directReports));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTeamMemberDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        boolean isDirectReport = manager != null && employee.getManager() != null && employee.getManager().getId().equals(manager.getId());
        boolean hasGlobalRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");

        if (!isDirectReport && !hasGlobalRead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: This employee does not report to you.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Team member details retrieved successfully", employee));
    }

    @GetMapping("/attendance")
    public ResponseEntity<?> getTeamAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity.ok(ApiResponse.success("Team attendance retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<Map<String, Object>> attendanceList = new ArrayList<>();

        for (Employee emp : directReports) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());

            Attendance todayAttendance = attendanceService.getTodayAttendance(emp).orElse(null);
            if (todayAttendance != null) {
                map.put("attendanceId", todayAttendance.getId());
                map.put("date", todayAttendance.getDate());
                map.put("status", todayAttendance.getStatus());
                map.put("punchInTime", todayAttendance.getPunchInTime());
                map.put("punchOutTime", todayAttendance.getPunchOutTime());
                map.put("notes", todayAttendance.getNotes());
            } else {
                map.put("attendanceId", null);
                map.put("date", java.time.LocalDate.now());
                map.put("status", "Absent");
                map.put("punchInTime", null);
                map.put("punchOutTime", null);
                map.put("notes", null);
            }
            attendanceList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success("Team attendance retrieved successfully", attendanceList));
    }

    @GetMapping("/schedules")
    public ResponseEntity<?> getTeamSchedules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity.ok(ApiResponse.success("Team schedules retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<Map<String, Object>> scheduleList = new ArrayList<>();

        for (Employee emp : directReports) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());
            try {
                map.put("todaySchedule", myScheduleService.getTodaySchedule(emp.getEmail()));
            } catch (Exception e) {
                map.put("todaySchedule", null);
            }
            scheduleList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success("Team schedules retrieved successfully", scheduleList));
    }

    @GetMapping("/performance")
    public ResponseEntity<?> getTeamPerformance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity.ok(ApiResponse.success("Team performance retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<Map<String, Object>> performanceList = new ArrayList<>();

        for (Employee emp : directReports) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());
            try {
                map.put("goals", performanceService.getGoalsByEmployee(emp.getId()));
                map.put("feedbacks", performanceService.getFeedbacksByEmployee(emp.getId()));
            } catch (Exception e) {
                map.put("goals", Collections.emptyList());
                map.put("feedbacks", Collections.emptyList());
            }
            performanceList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success("Team performance retrieved successfully", performanceList));
    }

    @GetMapping("/trainings")
    public ResponseEntity<?> getTeamTrainings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity.ok(ApiResponse.success("Team trainings retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<Map<String, Object>> trainingList = new ArrayList<>();

        for (Employee emp : directReports) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());
            try {
                map.put("enrollments", trainingService.getMyEnrollments(emp.getEmail()));
            } catch (Exception e) {
                map.put("enrollments", Collections.emptyList());
            }
            trainingList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success("Team trainings retrieved successfully", trainingList));
    }

    @GetMapping("/assets")
    public ResponseEntity<?> getTeamAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity.ok(ApiResponse.success("Team assets retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<Map<String, Object>> assetList = new ArrayList<>();

        for (Employee emp : directReports) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());
            try {
                map.put("assets", myAssetService.getAssignedAssets(emp, null, null, null, Pageable.unpaged()).getContent());
            } catch (Exception e) {
                map.put("assets", Collections.emptyList());
            }
            assetList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success("Team assets retrieved successfully", assetList));
    }

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }
}
