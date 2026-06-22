package com.example.ems.reports.repository;

import com.example.ems.reports.entity.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {
    List<ReportTemplate> findByCategory(String category);
}
