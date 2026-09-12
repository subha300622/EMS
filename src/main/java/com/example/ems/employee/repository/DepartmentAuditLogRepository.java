package com.example.ems.employee.repository;

import com.example.ems.employee.entity.DepartmentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DepartmentAuditLogRepository extends JpaRepository<DepartmentAuditLog, Long> {
    List<DepartmentAuditLog> findByDepartmentId(Long departmentId);
}
