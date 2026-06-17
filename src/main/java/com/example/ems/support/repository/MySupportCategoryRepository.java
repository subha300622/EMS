package com.example.ems.support.repository;

import com.example.ems.support.entity.MySupportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MySupportCategoryRepository extends JpaRepository<MySupportCategory, Long> {
    Optional<MySupportCategory> findByName(String name);
}
