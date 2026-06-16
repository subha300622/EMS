package com.example.ems.employee.repository;

import com.example.ems.employee.entity.MyDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyDocumentTypeRepository extends JpaRepository<MyDocumentType, Long> {
    Optional<MyDocumentType> findByCode(String code);
    List<MyDocumentType> findByCategoryId(Long categoryId);
}
