package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface MyAssetCategoryRepository extends JpaRepository<MyAssetCategory, Long> {
    Optional<MyAssetCategory> findByCode(String code);
}
