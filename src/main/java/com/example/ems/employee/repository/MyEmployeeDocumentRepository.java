package com.example.ems.employee.repository;

import com.example.ems.employee.entity.MyEmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyEmployeeDocumentRepository extends JpaRepository<MyEmployeeDocument, Long> {
    List<MyEmployeeDocument> findByEmployeeId(Long employeeId);
    Optional<MyEmployeeDocument> findByEmployeeIdAndDocumentTypeCode(Long employeeId, String code);
    Optional<MyEmployeeDocument> findByEmployeeIdAndDocumentTypeId(Long employeeId, Long typeId);
}
