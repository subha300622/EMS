package com.example.ems.employee.repository;

import com.example.ems.employee.entity.DepartmentTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentTransferRepository extends JpaRepository<DepartmentTransfer, Long> {
}
