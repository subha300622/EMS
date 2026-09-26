package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {

    Optional<AssetCategory> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<AssetCategory> findByIdAndOrganizationIdAndActiveTrue(Long id, Long organizationId);

    List<AssetCategory> findByOrganizationId(Long organizationId);

    List<AssetCategory> findByOrganizationIdAndActiveTrue(Long organizationId);

    boolean existsByOrganizationIdAndCategoryCodeIgnoreCase(Long organizationId, String categoryCode);

    boolean existsByOrganizationIdAndCategoryNameIgnoreCase(Long organizationId, String categoryName);

    boolean existsByOrganizationIdAndCategoryCodeIgnoreCaseAndIdNot(Long organizationId, String categoryCode, Long id);

    boolean existsByOrganizationIdAndCategoryNameIgnoreCaseAndIdNot(Long organizationId, String categoryName, Long id);
}
