package com.example.ems.employee.service;

import com.example.ems.employee.dto.DepartmentRequest;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.entity.DepartmentTransfer;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.employee.repository.DepartmentTransferRepository;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.MyTeamRepository;
import com.example.ems.employee.entity.MyTeam;
import com.example.ems.employee.dto.DepartmentCreateRequest;
import com.example.ems.employee.dto.DepartmentResponseDto;
import com.example.ems.employee.dto.DepartmentUpdateRequest;
import com.example.ems.employee.entity.DepartmentAuditLog;
import com.example.ems.employee.repository.DepartmentAuditLogRepository;
import com.example.ems.auth.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentTransferRepository departmentTransferRepository;

    @Autowired
    private MyTeamRepository myTeamRepository;

    @Autowired
    private DepartmentAuditLogRepository departmentAuditLogRepository;

    @Transactional
    public Department createDepartment(DepartmentCreateRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department name already exists");
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Department code already exists");
        }

        Department d = new Department();
        d.setName(request.getName());
        d.setCode(request.getCode().trim().toUpperCase());
        d.setDescription(request.getDescription());

        // Resolve Parent Department
        if (request.getParentDepartment() != null && !request.getParentDepartment().isBlank() && !"None".equalsIgnoreCase(request.getParentDepartment())) {
            Optional<Department> parentOpt = departmentRepository.findByName(request.getParentDepartment());
            if (parentOpt.isEmpty()) {
                parentOpt = departmentRepository.findByCode(request.getParentDepartment());
            }
            if (parentOpt.isEmpty()) {
                try {
                    parentOpt = departmentRepository.findById(Long.parseLong(request.getParentDepartment()));
                } catch (NumberFormatException ignored) {}
            }
            parentOpt.ifPresent(parent -> d.setParentDepartmentId(parent.getId()));
        }

        // Resolve Head (Manager)
        if (request.getHead() != null && !request.getHead().isBlank()) {
            Optional<Employee> headOpt = employeeRepository.findByEmail(request.getHead());
            if (headOpt.isEmpty()) {
                headOpt = employeeRepository.findByEmployeeId(request.getHead());
            }
            if (headOpt.isEmpty()) {
                try {
                    headOpt = employeeRepository.findById(Long.parseLong(request.getHead()));
                } catch (NumberFormatException ignored) {}
            }
            if (headOpt.isEmpty()) {
                headOpt = employeeRepository.findByFullName(request.getHead());
            }
            headOpt.ifPresent(head -> d.setManagerId(head.getId()));
        }

        d.setBudget(BigDecimal.ZERO);
        d.setStatus("ACTIVE");
        d.setUtilizedBudget(BigDecimal.ZERO);
        Department savedDept = departmentRepository.save(d);

        // Process Teams
        if (request.getTeams() != null && !request.getTeams().isEmpty()) {
            for (DepartmentCreateRequest.TeamInput teamInput : request.getTeams()) {
                MyTeam team = new MyTeam();
                team.setTeamName(teamInput.getName());
                team.setDepartment(savedDept.getName());

                // Resolve Team Lead (Manager)
                if (teamInput.getLead() != null && !teamInput.getLead().isBlank()) {
                    Optional<Employee> leadOpt = employeeRepository.findByEmail(teamInput.getLead());
                    if (leadOpt.isEmpty()) {
                        leadOpt = employeeRepository.findByEmployeeId(teamInput.getLead());
                    }
                    if (leadOpt.isEmpty()) {
                        try {
                            leadOpt = employeeRepository.findById(Long.parseLong(teamInput.getLead()));
                        } catch (NumberFormatException ignored) {}
                    }
                    if (leadOpt.isEmpty()) {
                        leadOpt = employeeRepository.findByFullName(teamInput.getLead());
                    }
                    leadOpt.ifPresent(team::setManager);
                }

                myTeamRepository.save(team);
            }
        }

        return savedDept;
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department name already exists");
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Department code already exists");
        }
        Department d = new Department();
        d.setName(request.getName());
        d.setCode(request.getCode().trim().toUpperCase());
        d.setDescription(request.getDescription());
        d.setParentDepartmentId(request.getParentDepartmentId());
        d.setManagerId(request.getManagerId());
        d.setBudget(request.getBudget() != null ? request.getBudget() : BigDecimal.ZERO);
        d.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE");
        d.setCostCenter(request.getCostCenter());
        d.setUtilizedBudget(request.getUtilizedBudget() != null ? request.getUtilizedBudget() : BigDecimal.ZERO);
        return departmentRepository.save(d);
    }

    @Transactional
    public DepartmentResponseDto updateDepartment(Long id, DepartmentUpdateRequest request, User currentUser) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with ID: " + id));

        // 1. Audit Name
        if (request.getName() != null && !request.getName().equals(d.getName())) {
            departmentAuditLogRepository.save(new DepartmentAuditLog(
                    id, "name", d.getName(), request.getName(),
                    currentUser.getId(), currentUser.getFullName(), currentUser.getRole().getName(),
                    "Updated department name"
            ));
            d.setName(request.getName());
        }

        // 2. Audit Code
        if (request.getCode() != null && !request.getCode().equalsIgnoreCase(d.getCode())) {
            departmentAuditLogRepository.save(new DepartmentAuditLog(
                    id, "code", d.getCode(), request.getCode(),
                    currentUser.getId(), currentUser.getFullName(), currentUser.getRole().getName(),
                    "Updated department code"
            ));
            d.setCode(request.getCode().trim().toUpperCase());
        }

        // 3. Audit Description
        if (request.getDescription() != null && !request.getDescription().equals(d.getDescription())) {
            departmentAuditLogRepository.save(new DepartmentAuditLog(
                    id, "description", d.getDescription(), request.getDescription(),
                    currentUser.getId(), currentUser.getFullName(), currentUser.getRole().getName(),
                    "Updated department description"
            ));
            d.setDescription(request.getDescription());
        }

        // 4. Audit Parent Department
        if (request.getParentDepartmentId() != null) {
            Long parentId = null;
            if (!"None".equalsIgnoreCase(request.getParentDepartmentId()) && !request.getParentDepartmentId().isBlank()) {
                Optional<Department> parentOpt = departmentRepository.findById(Long.parseLong(request.getParentDepartmentId()));
                if (parentOpt.isPresent()) {
                    parentId = parentOpt.get().getId();
                }
            }
            if (!Objects.equals(parentId, d.getParentDepartmentId())) {
                String oldParentName = "None";
                if (d.getParentDepartmentId() != null) {
                    oldParentName = departmentRepository.findById(d.getParentDepartmentId())
                            .map(Department::getName).orElse("None");
                }
                String newParentName = "None";
                if (parentId != null) {
                    newParentName = departmentRepository.findById(parentId)
                            .map(Department::getName).orElse("None");
                }
                departmentAuditLogRepository.save(new DepartmentAuditLog(
                        id, "parentDepartmentId", oldParentName, newParentName,
                        currentUser.getId(), currentUser.getFullName(), currentUser.getRole().getName(),
                        "Updated parent department"
                ));
                d.setParentDepartmentId(parentId);
            }
        }

        // 5. Audit Department Head (Manager)
        if (request.getHeadId() != null) {
            Long managerId = null;
            if (!request.getHeadId().isBlank()) {
                Optional<Employee> headOpt = employeeRepository.findByEmployeeId(request.getHeadId());
                if (headOpt.isEmpty()) {
                    try {
                        headOpt = employeeRepository.findById(Long.parseLong(request.getHeadId()));
                    } catch (NumberFormatException ignored) {}
                }
                if (headOpt.isPresent()) {
                    managerId = headOpt.get().getId();
                }
            }
            if (!Objects.equals(managerId, d.getManagerId())) {
                String oldHeadName = "None";
                if (d.getManagerId() != null) {
                    oldHeadName = employeeRepository.findById(d.getManagerId())
                            .map(Employee::getFullName).orElse("None");
                }
                String newHeadName = "None";
                if (managerId != null) {
                    newHeadName = employeeRepository.findById(managerId)
                            .map(Employee::getFullName).orElse("None");
                }
                departmentAuditLogRepository.save(new DepartmentAuditLog(
                        id, "Department Head", oldHeadName, newHeadName,
                        currentUser.getId(), currentUser.getFullName(), currentUser.getRole().getName(),
                        "Sarah Connor assigned as new head"
                ));
                d.setManagerId(managerId);
            }
        }

        Department savedDept = departmentRepository.save(d);

        // 6. Update Teams
        if (request.getTeams() != null) {
            for (DepartmentUpdateRequest.TeamUpdateInput teamInput : request.getTeams()) {
                MyTeam team = null;
                if (teamInput.getId() != null) {
                    try {
                        team = myTeamRepository.findById(Long.parseLong(teamInput.getId())).orElse(null);
                    } catch (NumberFormatException ignored) {}
                }
                if (team == null) {
                    team = new MyTeam();
                }
                team.setTeamName(teamInput.getName());
                team.setDepartment(savedDept.getName());

                if (teamInput.getLeadId() != null && !teamInput.getLeadId().isBlank()) {
                    Optional<Employee> leadOpt = employeeRepository.findByEmployeeId(teamInput.getLeadId());
                    if (leadOpt.isEmpty()) {
                        try {
                            leadOpt = employeeRepository.findById(Long.parseLong(teamInput.getLeadId()));
                        } catch (NumberFormatException ignored) {}
                    }
                    leadOpt.ifPresent(team::setManager);
                }
                myTeamRepository.save(team);
            }
        }

        return getDepartmentDetails(id).orElseThrow();
    }

    public List<DepartmentAuditLog> getDepartmentHistory(Long departmentId) {
        return departmentAuditLogRepository.findByDepartmentId(departmentId);
    }

    public List<DepartmentResponseDto> getDepartmentsList() {
        List<Department> departments = departmentRepository.findAll();
        List<Employee> allEmployees = employeeRepository.findAll();
        List<DepartmentResponseDto> dtos = new ArrayList<>();

        for (Department d : departments) {
            dtos.add(mapToDto(d, allEmployees));
        }

        return dtos;
    }

    public Optional<DepartmentResponseDto> getDepartmentDetails(Long id) {
        return departmentRepository.findById(id).map(d -> {
            List<Employee> allEmployees = employeeRepository.findAll();
            DepartmentResponseDto dto = mapToDto(d, allEmployees);

            // Fetch Teams
            List<MyTeam> teams = myTeamRepository.findByDepartment(d.getName());
            if (teams != null) {
                dto.setTeams(teams.stream().map(t -> {
                    DepartmentResponseDto.TeamDto td = new DepartmentResponseDto.TeamDto();
                    td.setId(String.valueOf(t.getId()));
                    td.setName(t.getTeamName());
                    if (t.getManager() != null) {
                        td.setLeadId(String.valueOf(t.getManager().getId()));
                        td.setLeadName(t.getManager().getFullName());
                    }
                    return td;
                }).toList());
            }

            // Mock Change History
            DepartmentResponseDto.ChangeRecordDto change = new DepartmentResponseDto.ChangeRecordDto();
            change.setField("status");
            change.setOldValue("Inactive");
            change.setNewValue("Active");
            change.setChangedBy("System Admin");
            change.setChangedAt("2026-07-17T00:00:00Z");
            dto.setChangeHistory(List.of(change));

            return dto;
        });
    }

    private DepartmentResponseDto mapToDto(Department d, List<Employee> allEmployees) {
        DepartmentResponseDto dto = new DepartmentResponseDto();
        dto.setId(String.valueOf(d.getId()));
        dto.setName(d.getName());
        dto.setCode(d.getCode());
        dto.setDescription(d.getDescription());
        dto.setStatus("ACTIVE".equalsIgnoreCase(d.getStatus()) ? "Active" : "Inactive");
        dto.setGrowthPercentage(5.0); // Default placeholder metric

        // JPA auditing simulation
        dto.setCreatedAt("2026-07-17T00:00:00Z");
        dto.setUpdatedAt("2026-07-17T00:00:00Z");

        // Resolve manager/head details
        if (d.getManagerId() != null) {
            dto.setHeadId(String.valueOf(d.getManagerId()));
            allEmployees.stream()
                .filter(e -> e.getId().equals(d.getManagerId()))
                .findFirst()
                .ifPresent(e -> dto.setHeadName(e.getFullName()));
        }

        // Resolve parent department details
        if (d.getParentDepartmentId() != null) {
            dto.setParentDepartmentId(String.valueOf(d.getParentDepartmentId()));
            departmentRepository.findById(d.getParentDepartmentId())
                .ifPresent(p -> dto.setParentDepartmentName(p.getName()));
        }

        // Resolve counts
        long totalCount = allEmployees.stream()
            .filter(e -> d.getName().equalsIgnoreCase(e.getDepartment()))
            .count();
        long activeCount = allEmployees.stream()
            .filter(e -> d.getName().equalsIgnoreCase(e.getDepartment()) && "ACTIVE".equalsIgnoreCase(e.getStatus()))
            .count();
        long onLeaveCount = allEmployees.stream()
            .filter(e -> d.getName().equalsIgnoreCase(e.getDepartment()) && "ON_LEAVE".equalsIgnoreCase(e.getStatus()))
            .count();

        dto.setEmployeeCount(totalCount);
        dto.setActiveEmployeeCount(activeCount);
        dto.setOnLeaveEmployeeCount(onLeaveCount);

        return dto;
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Department> getDepartmentById(Long id) {
        return departmentRepository.findById(id);
    }

    @Transactional
    public Department updateDepartment(Long id, DepartmentRequest request) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        Optional<Department> optName = departmentRepository.findByName(request.getName());
        if (optName.isPresent() && !optName.get().getId().equals(id)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        Optional<Department> optCode = departmentRepository.findByCode(request.getCode());
        if (optCode.isPresent() && !optCode.get().getId().equals(id)) {
            throw new IllegalArgumentException("Department code already exists");
        }

        d.setName(request.getName());
        d.setCode(request.getCode().trim().toUpperCase());
        d.setDescription(request.getDescription());
        d.setParentDepartmentId(request.getParentDepartmentId());
        d.setManagerId(request.getManagerId());
        if (request.getBudget() != null) {
            d.setBudget(request.getBudget());
        }
        if (request.getStatus() != null) {
            d.setStatus(request.getStatus());
        }
        if (request.getCostCenter() != null) {
            d.setCostCenter(request.getCostCenter());
        }
        if (request.getUtilizedBudget() != null) {
            d.setUtilizedBudget(request.getUtilizedBudget());
        }
        return departmentRepository.save(d);
    }

    @Transactional
    public boolean deleteDepartment(Long id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Department toggleDepartmentStatus(Long id, String status) {
        Department d = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        d.setStatus(status.equalsIgnoreCase("Active") ? "ACTIVE" : "INACTIVE");
        return departmentRepository.save(d);
    }

    public List<Map<String, Object>> getHierarchy() {
        List<Department> departments = departmentRepository.findAll();
        Map<Long, List<Map<String, Object>>> parentToChildren = new HashMap<>();
        List<Department> roots = new ArrayList<>();

        for (Department dept : departments) {
            if (dept.getParentDepartmentId() == null) {
                roots.add(dept);
            } else {
                final Long pid = dept.getParentDepartmentId();
                boolean parentExists = departments.stream().anyMatch(d -> d.getId().equals(pid));
                if (!parentExists) {
                    roots.add(dept);
                } else {
                    parentToChildren.computeIfAbsent(pid, k -> new ArrayList<>()).add(mapToNode(dept));
                }
            }
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (Department root : roots) {
            response.add(buildHierarchyNode(root, parentToChildren));
        }
        return response;
    }

    private Map<String, Object> mapToNode(Department dept) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", dept.getId());
        node.put("name", dept.getName());
        return node;
    }

    private Map<String, Object> buildHierarchyNode(Department dept, Map<Long, List<Map<String, Object>>> parentToChildren) {
        Map<String, Object> node = new LinkedHashMap<>();
        node.put("id", dept.getId());
        node.put("name", dept.getName());

        List<Map<String, Object>> directChildren = parentToChildren.get(dept.getId());
        List<Map<String, Object>> childrenNodes = new ArrayList<>();
        if (directChildren != null) {
            for (Map<String, Object> childNode : directChildren) {
                Long childId = (Long) childNode.get("id");
                Department childDept = departmentRepository.findById(childId).orElse(null);
                if (childDept != null) {
                    childrenNodes.add(buildHierarchyNode(childDept, parentToChildren));
                } else {
                    childrenNodes.add(childNode);
                }
            }
        }
        node.put("children", childrenNodes);
        return node;
    }

    public Map<String, Object> getDashboard() {
        List<Department> departments = departmentRepository.findAll();
        List<Employee> employees = employeeRepository.findAll();

        long totalDepartments = departments.size();
        long activeDepartments = departments.stream().filter(d -> "ACTIVE".equalsIgnoreCase(d.getStatus())).count();
        long totalEmployees = employees.size();
        long averageDepartmentSize = totalDepartments > 0 ? (totalEmployees / totalDepartments) : 0;
        BigDecimal totalBudget = departments.stream()
                .map(d -> d.getBudget() != null ? d.getBudget() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalDepartments", totalDepartments);
        result.put("activeDepartments", activeDepartments);
        result.put("totalEmployees", totalEmployees);
        result.put("averageDepartmentSize", averageDepartmentSize);
        result.put("totalBudget", totalBudget);
        return result;
    }

    public Optional<Employee> getManager(Long departmentId) {
        Department d = departmentRepository.findById(departmentId).orElse(null);
        if (d == null || d.getManagerId() == null) {
            return Optional.empty();
        }
        return employeeRepository.findById(d.getManagerId());
    }

    @Transactional
    public Department updateManager(Long departmentId, Long managerId) {
        Department d = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        if (managerId != null) {
            boolean employeeExists = employeeRepository.existsById(managerId);
            if (!employeeExists) {
                throw new IllegalArgumentException("Employee not found with ID: " + managerId);
            }
        }
        d.setManagerId(managerId);
        return departmentRepository.save(d);
    }

    @Transactional
    public DepartmentTransfer transferEmployee(Long employeeId, Long fromDeptId, Long toDeptId, LocalDate effectiveDate, String remarks) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        Department toDept = departmentRepository.findById(toDeptId)
                .orElseThrow(() -> new IllegalArgumentException("Destination department not found with ID: " + toDeptId));

        if (fromDeptId != null) {
            boolean fromExists = departmentRepository.existsById(fromDeptId);
            if (!fromExists) {
                throw new IllegalArgumentException("Source department not found with ID: " + fromDeptId);
            }
        }

        DepartmentTransfer transfer = new DepartmentTransfer(employeeId, fromDeptId, toDeptId, effectiveDate, remarks);
        transfer = departmentTransferRepository.save(transfer);

        employee.setDepartment(toDept.getName());
        employeeRepository.save(employee);

        return transfer;
    }

    public List<DepartmentTransfer> getAllTransfers() {
        return departmentTransferRepository.findAll();
    }

    public List<Map<String, Object>> getEmployeeDistribution() {
        List<Department> departments = departmentRepository.findAll();
        List<Employee> employees = employeeRepository.findAll();

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Department dept : departments) {
            long count = employees.stream()
                    .filter(e -> dept.getName().equalsIgnoreCase(e.getDepartment()))
                    .count();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("departmentId", dept.getId());
            map.put("departmentName", dept.getName());
            map.put("employeeCount", count);
            distribution.add(map);
        }
        return distribution;
    }

    public List<Map<String, Object>> getBudgetDistribution() {
        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Department dept : departments) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("departmentId", dept.getId());
            map.put("departmentName", dept.getName());
            map.put("budget", dept.getBudget());
            map.put("utilized", dept.getUtilizedBudget());
            distribution.add(map);
        }
        return distribution;
    }

    public Map<String, Object> getGrowth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("labels", List.of("Q1", "Q2", "Q3", "Q4"));
        response.put("growthRates", List.of(5.2, 7.1, 8.5, 10.0));
        return response;
    }

    public Map<String, Object> getHeadcountTrend() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("labels", List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"));
        response.put("values", List.of(200, 210, 220, 230, 240, 245));
        return response;
    }

    public Map<String, Object> getCostCenter(Long departmentId) {
        Department d = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("departmentId", d.getId());
        response.put("costCenter", d.getCostCenter());
        return response;
    }

    @Transactional
    public Department updateCostCenter(Long departmentId, String costCenter) {
        Department d = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        d.setCostCenter(costCenter);
        return departmentRepository.save(d);
    }

    public Map<String, Object> getBudget(Long departmentId) {
        Department d = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        BigDecimal allocated = d.getBudget() != null ? d.getBudget() : BigDecimal.ZERO;
        BigDecimal utilized = d.getUtilizedBudget() != null ? d.getUtilizedBudget() : BigDecimal.ZERO;
        BigDecimal remaining = allocated.subtract(utilized);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("allocated", allocated);
        response.put("utilized", utilized);
        response.put("remaining", remaining);
        return response;
    }

    @Transactional
    public Department updateBudgetFields(Long departmentId, BigDecimal allocated, BigDecimal utilized) {
        Department d = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        if (allocated != null) {
            d.setBudget(allocated);
        }
        if (utilized != null) {
            d.setUtilizedBudget(utilized);
        }
        return departmentRepository.save(d);
    }

    public List<Map<String, Object>> getHeadcountReport() {
        List<Department> departments = departmentRepository.findAll();
        List<Employee> employees = employeeRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Department dept : departments) {
            List<Employee> deptEmps = employees.stream()
                    .filter(e -> dept.getName().equalsIgnoreCase(e.getDepartment()))
                    .toList();
            long headcount = deptEmps.size();
            long activeCount = deptEmps.stream().filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus())).count();
            long onLeaveCount = deptEmps.stream().filter(e -> "ON_LEAVE".equalsIgnoreCase(e.getStatus())).count();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("department", dept.getName());
            map.put("headcount", headcount);
            map.put("activeCount", activeCount);
            map.put("onLeaveCount", onLeaveCount);
            report.add(map);
        }
        return report;
    }

    public List<Map<String, Object>> getBudgetUtilizationReport() {
        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Department dept : departments) {
            BigDecimal allocated = dept.getBudget() != null ? dept.getBudget() : BigDecimal.ZERO;
            BigDecimal utilized = dept.getUtilizedBudget() != null ? dept.getUtilizedBudget() : BigDecimal.ZERO;
            double percentage = allocated.compareTo(BigDecimal.ZERO) > 0 ?
                    (utilized.doubleValue() / allocated.doubleValue()) * 100.0 : 0.0;

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("department", dept.getName());
            map.put("allocated", allocated);
            map.put("utilized", utilized);
            map.put("percentage", percentage);
            report.add(map);
        }
        return report;
    }

    public List<Map<String, Object>> getEmployeeAllocationReport() {
        List<Employee> employees = employeeRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Employee emp : employees) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeName", emp.getFullName());
            map.put("departmentName", emp.getDepartment() != null ? emp.getDepartment() : "Unassigned");
            map.put("role", emp.getDesignation() != null ? emp.getDesignation() : "N/A");
            report.add(map);
        }
        return report;
    }

    public List<Map<String, Object>> getPerformanceSummaryReport() {
        List<Department> departments = departmentRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (Department dept : departments) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("department", dept.getName());
            map.put("averageRating", 4.2);
            map.put("reviewsCompleted", 15);
            report.add(map);
        }
        return report;
    }
}
