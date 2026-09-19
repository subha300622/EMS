package com.example.ems.support.repository;

import com.example.ems.support.entity.SupportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SupportTemplateRepository extends JpaRepository<SupportTemplate, Long> {
    List<SupportTemplate> findByCategoryIgnoreCase(String category);
}
