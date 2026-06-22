package com.example.ems.employee.repository;

import com.example.ems.employee.entity.DepartmentTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentTransferRepository extends JpaRepository<DepartmentTransfer, Long> {
    List<DepartmentTransfer> findByEmployeeId(Long employeeId);
}
