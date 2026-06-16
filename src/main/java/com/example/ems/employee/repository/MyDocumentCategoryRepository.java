package com.example.ems.employee.repository;

import com.example.ems.employee.entity.MyDocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyDocumentCategoryRepository extends JpaRepository<MyDocumentCategory, Long> {
    Optional<MyDocumentCategory> findByName(String name);
}
