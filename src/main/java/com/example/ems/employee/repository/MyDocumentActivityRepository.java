package com.example.ems.employee.repository;

import com.example.ems.employee.entity.MyDocumentActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyDocumentActivityRepository extends JpaRepository<MyDocumentActivity, Long> {
    Page<MyDocumentActivity> findByEmployeeId(Long employeeId, Pageable pageable);
}
