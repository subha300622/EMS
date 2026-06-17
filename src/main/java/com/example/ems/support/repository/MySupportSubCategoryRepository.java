package com.example.ems.support.repository;

import com.example.ems.support.entity.MySupportCategory;
import com.example.ems.support.entity.MySupportSubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MySupportSubCategoryRepository extends JpaRepository<MySupportSubCategory, Long> {
    List<MySupportSubCategory> findByCategoryId(Long categoryId);
    List<MySupportSubCategory> findByCategory(MySupportCategory category);
}
