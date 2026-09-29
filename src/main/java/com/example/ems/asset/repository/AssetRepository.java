package com.example.ems.asset.repository;

import com.example.ems.asset.entity.Asset;
import com.example.ems.asset.entity.AssetStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


@Repository
public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByIdAndOrganizationIdAndDeletedFalse(Long id, Long organizationId);

    Optional<Asset> findByAssetCodeIgnoreCaseAndDeletedFalse(String assetCode);

    Optional<Asset> findByOrganizationIdAndAssetCodeIgnoreCaseAndDeletedFalse(Long organizationId, String assetCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.id = :id AND a.organizationId = :orgId AND a.deleted = false")
    Optional<Asset> findByIdAndOrganizationIdWithLock(@Param("id") Long id, @Param("orgId") Long orgId);

    List<Asset> findByOrganizationIdAndDeletedFalse(Long organizationId);

    List<Asset> findByOrganizationIdAndStatusAndDeletedFalse(Long organizationId, AssetStatus status);

    boolean existsByOrganizationIdAndAssetCodeIgnoreCase(Long organizationId, String assetCode);

    boolean existsByOrganizationIdAndSerialNumberIgnoreCaseAndDeletedFalse(Long organizationId, String serialNumber);

    boolean existsByCategoryIdAndDeletedFalse(Long categoryId);

    boolean existsByLocationIdAndDeletedFalse(Long locationId);
}
