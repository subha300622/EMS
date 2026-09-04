package com.example.ems.support.repository;

import com.example.ems.support.entity.MySupportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface MySupportCategoryRepository extends JpaRepository<MySupportCategory, Long>, JpaSpecificationExecutor<MySupportCategory> {
    Optional<MySupportCategory> findByName(String name);
    Optional<MySupportCategory> findByNameIgnoreCase(String name);
}
