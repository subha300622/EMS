package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetMaintenance;
import com.example.ems.asset.entity.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetMaintenanceRepository extends JpaRepository<AssetMaintenance, Long> {

    Optional<AssetMaintenance> findByIdAndOrganizationId(Long id, Long organizationId);

    List<AssetMaintenance> findByAssetIdAndOrganizationId(Long assetId, Long organizationId);

    List<AssetMaintenance> findByOrganizationIdAndStatus(Long organizationId, MaintenanceStatus status);

    boolean existsByAssetIdAndStatusIn(Long assetId, List<MaintenanceStatus> statuses);
}
