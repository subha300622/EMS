package com.example.ems.employee.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ems.asset.service.MyAssetService;
import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.service.AttendanceService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.performance.service.PerformanceService;
import com.example.ems.schedule.service.MyScheduleService;
import com.example.ems.security.service.JwtService;
import com.example.ems.training.service.TrainingAssignmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.ems.employee.dto.TeamManagementDtos;
import java.time.LocalDate;

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
    public ResponseEntity<ApiResponse<List<Employee>>> getTeamDirectory(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
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
    public ResponseEntity<ApiResponse<Employee>> getTeamMemberDetails(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        boolean isDirectReport = manager != null && employee.getManager() != null
                && employee.getManager().getId().equals(manager.getId());
        boolean hasGlobalRead = roleService.hasPermission(currentUser.getWorkEmail(), "employee.read");

        if (!isDirectReport && !hasGlobalRead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: This employee does not report to you.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Team member details retrieved successfully", employee));
    }

    @Operation(summary = "Get Team Attendance", description = "Retrieves today's punch status and shift notes for all team members.")
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<List<TeamManagementDtos.TeamAttendanceItemDto>>> getTeamAttendance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team attendance retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<TeamManagementDtos.TeamAttendanceItemDto> attendanceList = new ArrayList<>();

        for (Employee emp : directReports) {
            Attendance todayAttendance = attendanceService.getTodayAttendance(emp).orElse(null);
            if (todayAttendance != null) {
                attendanceList.add(new TeamManagementDtos.TeamAttendanceItemDto(
                        emp.getId(), emp.getFullName(), emp.getEmail(),
                        todayAttendance.getId(), todayAttendance.getDate(), todayAttendance.getStatus(),
                        todayAttendance.getPunchInTime(), todayAttendance.getPunchOutTime(), todayAttendance.getNotes()
                ));
            } else {
                attendanceList.add(new TeamManagementDtos.TeamAttendanceItemDto(
                        emp.getId(), emp.getFullName(), emp.getEmail(),
                        null, LocalDate.now(), "Absent", null, null, null
                ));
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Team attendance retrieved successfully", attendanceList));
    }

    @Operation(summary = "Get Team Schedules", description = "Retrieves shift scheduling and work timings for the team members for today.")
    @GetMapping("/schedules")
    public ResponseEntity<ApiResponse<List<TeamManagementDtos.TeamScheduleItemDto>>> getTeamSchedules(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team schedules retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<TeamManagementDtos.TeamScheduleItemDto> scheduleList = new ArrayList<>();

        for (Employee emp : directReports) {
            Object sched = null;
            try {
                sched = myScheduleService.getTodaySchedule(emp.getEmail());
            } catch (Exception e) {
                sched = null;
            }
            scheduleList.add(new TeamManagementDtos.TeamScheduleItemDto(
                    emp.getId(), emp.getFullName(), emp.getEmail(), sched
            ));
        }

        return ResponseEntity.ok(ApiResponse.success("Team schedules retrieved successfully", scheduleList));
    }

    @Operation(summary = "Get Team Performance Summary", description = "Retrieves active performance goals and feedback loops for all team members.")
    @GetMapping("/performance")
    public ResponseEntity<ApiResponse<List<TeamManagementDtos.TeamPerformanceItemDto>>> getTeamPerformance(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team performance retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<TeamManagementDtos.TeamPerformanceItemDto> performanceList = new ArrayList<>();

        for (Employee emp : directReports) {
            List<?> goals;
            List<?> feedbacks;
            try {
                goals = performanceService.getGoalsByEmployee(emp.getId());
                feedbacks = performanceService.getFeedbacksByEmployee(emp.getId());
            } catch (Exception e) {
                goals = Collections.emptyList();
                feedbacks = Collections.emptyList();
            }
            performanceList.add(new TeamManagementDtos.TeamPerformanceItemDto(
                    emp.getId(), emp.getFullName(), emp.getEmail(), goals, feedbacks
            ));
        }

        return ResponseEntity.ok(ApiResponse.success("Team performance retrieved successfully", performanceList));
    }

    @Operation(summary = "Get Team Trainings Status", description = "Retrieves course enrollment and training completion logs for all team members.")
    @GetMapping("/trainings")
    public ResponseEntity<ApiResponse<List<TeamManagementDtos.TeamTrainingItemDto>>> getTeamTrainings(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team trainings retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<TeamManagementDtos.TeamTrainingItemDto> trainingList = new ArrayList<>();

        for (Employee emp : directReports) {
            List<?> enrollments;
            try {
                enrollments = trainingService.getMyTrainings(emp.getEmail());
            } catch (Exception e) {
                enrollments = Collections.emptyList();
            }
            trainingList.add(new TeamManagementDtos.TeamTrainingItemDto(
                    emp.getId(), emp.getFullName(), emp.getEmail(), enrollments
            ));
        }

        return ResponseEntity.ok(ApiResponse.success("Team trainings retrieved successfully", trainingList));
    }

    @Operation(summary = "Get Team Assets Allocation", description = "Retrieves hardware and software assets allocated to the team members.")
    @GetMapping("/assets")
    public ResponseEntity<ApiResponse<List<TeamManagementDtos.TeamAssetItemDto>>> getTeamAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "team.read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'team.read' permission.", "AUTH_002"));
        }

        Employee manager = employeeRepository.findByEmail(currentUser.getWorkEmail()).orElse(null);
        if (manager == null) {
            return ResponseEntity
                    .ok(ApiResponse.success("Team assets retrieved successfully", Collections.emptyList()));
        }

        List<Employee> directReports = employeeRepository.findByManagerId(manager.getId());
        List<TeamManagementDtos.TeamAssetItemDto> assetList = new ArrayList<>();

        for (Employee emp : directReports) {
            List<?> assets;
            try {
                assets = myAssetService.getAssignedAssets(emp, null, null, null, Pageable.unpaged()).getContent();
            } catch (Exception e) {
                assets = Collections.emptyList();
            }
            assetList.add(new TeamManagementDtos.TeamAssetItemDto(
                    emp.getId(), emp.getFullName(), emp.getEmail(), assets
            ));
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
