package com.example.ems.attendance.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.employee.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TeamContextFactory {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public TeamContext buildContext() {
        List<Employee> allEmployees = employeeRepository.findAll();
        List<Department> allDepartments = departmentRepository.findAll();

        Map<Long, Employee> employeeMap = allEmployees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        Map<String, Department> departmentMap = allDepartments.stream()
                .collect(Collectors.toMap(d -> d.getName().toLowerCase(), d -> d, (d1, d2) -> d1));

        Map<String, Employee> departmentManagerMap = allDepartments.stream()
                .filter(d -> d.getManagerId() != null && employeeMap.containsKey(d.getManagerId()))
                .collect(Collectors.toMap(
                        d -> d.getName().toLowerCase(),
                        d -> employeeMap.get(d.getManagerId()),
                        (m1, m2) -> m1
                ));

        return new TeamContext(employeeMap, departmentMap, departmentManagerMap);
    }

    public static class TeamContext {
        private final Map<Long, Employee> employeeMap;
        private final Map<String, Department> departmentMap;
        private final Map<String, Employee> departmentManagerMap;

        public TeamContext(Map<Long, Employee> employeeMap,
                           Map<String, Department> departmentMap,
                           Map<String, Employee> departmentManagerMap) {
            this.employeeMap = employeeMap;
            this.departmentMap = departmentMap;
            this.departmentManagerMap = departmentManagerMap;
        }

        public Employee getEmployee(Long id) {
            return employeeMap.get(id);
        }

        public Department getDepartmentByName(String name) {
            return name == null ? null : departmentMap.get(name.toLowerCase());
        }

        public Employee getDepartmentManager(String deptName) {
            return deptName == null ? null : departmentManagerMap.get(deptName.toLowerCase());
        }

        public Employee getResolvedManager(Employee e) {
            if (e.getManager() != null) {
                return e.getManager();
            }
            return getDepartmentManager(e.getDepartment());
        }
    }
}
