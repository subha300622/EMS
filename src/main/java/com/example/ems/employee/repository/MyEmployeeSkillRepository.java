package com.example.ems.employee.repository;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.entity.MyEmployeeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MyEmployeeSkillRepository extends JpaRepository<MyEmployeeSkill, Long> {
    List<MyEmployeeSkill> findByEmployee(Employee employee);
    List<MyEmployeeSkill> findByEmployeeId(Long employeeId);
}
