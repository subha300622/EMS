package com.example.ems.employee.controller;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.dto.OrgChartNodeDto;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin("*")
@Tag(name = "Organization Management")
public class OrgChartController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/organization-chart")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOrganizationChart(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.directory.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.directory.read' permission.",
                            "AUTH_002"));
        }

        List<Employee> allEmployees = employeeRepository.findAll();
        List<Employee> roots = allEmployees.stream()
                .filter(e -> e.getManager() == null)
                .collect(Collectors.toList());

        List<OrgChartNodeDto> orgChart = roots.stream()
                .map(r -> buildHierarchy(r, allEmployees, new HashSet<>()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Organization chart retrieved successfully", orgChart));
    }

    @GetMapping("/organization-chart/{employeeId}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getOrganizationChartForEmployee(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long employeeId){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.directory.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.directory.read' permission.",
                            "AUTH_002"));
        }

        Employee rootEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (rootEmployee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + employeeId, "EMP_002"));
        }

        List<Employee> allEmployees = employeeRepository.findAll();
        OrgChartNodeDto chart = buildHierarchy(rootEmployee, allEmployees, new HashSet<>());

        return ResponseEntity.ok(ApiResponse.success("Organization chart for employee retrieved successfully", chart));
    }

    @GetMapping("/employees/{id}/reporting-chain")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getReportingChain(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "employee.directory.read")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'employee.directory.read' permission.",
                            "AUTH_002"));
        }

        Employee employee = employeeRepository.findById(id).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + id, "EMP_002"));
        }

        List<Map<String, Object>> chain = new ArrayList<>();
        Employee current = employee;
        Set<Long> visited = new HashSet<>();

        while (current != null && !visited.contains(current.getId())) {
            visited.add(current.getId());
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("id", current.getId());
            node.put("employeeId", current.getEmployeeId());
            node.put("fullName", current.getFullName());
            node.put("designation", current.getDesignation());
            node.put("email", current.getEmail());
            node.put("department", current.getDepartment());
            chain.add(node);
            current = current.getManager();
        }

        return ResponseEntity.ok(ApiResponse.success("Reporting chain retrieved successfully", chain));
    }

    private OrgChartNodeDto buildHierarchy(Employee employee, List<Employee> allEmployees, Set<Long> visited) {
        visited.add(employee.getId());
        OrgChartNodeDto node = new OrgChartNodeDto(
                employee.getId(),
                employee.getEmployeeId(),
                employee.getFullName(),
                employee.getDesignation(),
                employee.getEmail(),
                employee.getProfileImage(),
                employee.getDepartment());

        List<Employee> children = allEmployees.stream()
                .filter(e -> e.getManager() != null && e.getManager().getId().equals(employee.getId()))
                .collect(Collectors.toList());

        for (Employee child : children) {
            if (!visited.contains(child.getId())) {
                node.addChild(buildHierarchy(child, allEmployees, visited));
            }
        }

        return node;
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
