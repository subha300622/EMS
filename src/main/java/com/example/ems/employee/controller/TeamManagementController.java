package com.example.ems.employee.controller;

import java.util.List;
import java.util.Map;

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
import com.example.ems.training.service.TrainingAssignmentService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/team")
@CrossOrigin("*")
@Tag(name = "Organization Directory")
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
    private TrainingAssignmentService trainingService;

    @Autowired
    private MyAssetService myAssetService;

    @Operation(summary = "Get Team Directory", description = "Retrieves profiles of all direct reports reporting to the authenticated manager.")
    @GetMapping
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamDirectory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team directory retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        return ResponseEntity.ok(ApiResponse.success("Team directory retrieved successfully", directReports));
    }

    @Operation(summary = "Get Team Member Details", description = "Retrieves detailed profile metadata for a specific direct report.")
    @GetMapping("/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamMemberDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        boolean isDirectReport = manager != null && employee.getManager() != null
                && employee.getManager().getId().equals(manager.getId());
        boolean hasGlobalRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");

        if (!isDirectReport && !hasGlobalRead) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: This employee does not report to you.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Team member details retrieved successfully", employee));
    }

    @Operation(summary = "Get Team Attendance", description = "Retrieves today's punch status and shift notes for all team members.")
    @GetMapping("/attendance")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team attendance retrieved successfully", Collections.emptyList()));
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

    @Operation(summary = "Get Team Schedules", description = "Retrieves shift scheduling and work timings for the team members for today.")
    @GetMapping("/schedules")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamSchedules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team schedules retrieved successfully", Collections.emptyList()));
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

    @Operation(summary = "Get Team Performance Summary", description = "Retrieves active performance goals and feedback loops for all team members.")
    @GetMapping("/performance")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamPerformance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team performance retrieved successfully", Collections.emptyList()));
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

    @Operation(summary = "Get Team Trainings Status", description = "Retrieves course enrollment and training completion logs for all team members.")
    @GetMapping("/trainings")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamTrainings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team trainings retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<Map<String, Object>> trainingList = new ArrayList<>();

        for (Employee emp : directReports) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());
            try {
                map.put("enrollments", trainingService.getMyTrainings(emp.getEmail()));
            } catch (Exception e) {
                map.put("enrollments", Collections.emptyList());
            }
            trainingList.add(map);
        }

        return ResponseEntity.ok(ApiResponse.success("Team trainings retrieved successfully", trainingList));
    }

    @Operation(summary = "Get Team Assets Allocation", description = "Retrieves hardware and software assets allocated to the team members.")
    @GetMapping("/assets")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getTeamAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team assets retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<Map<String, Object>> assetList = new ArrayList<>();

        for (Employee emp : directReports) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", emp.getId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());
            try {
                map.put("assets",
                        myAssetService.getAssignedAssets(emp, null, null, null, Pageable.unpaged()).getContent());
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
