package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {

    List<AssetHistory> findByAssetIdAndOrganizationIdOrderByPerformedAtDesc(Long assetId, Long organizationId);

    List<AssetHistory> findByOrganizationIdOrderByPerformedAtDesc(Long organizationId);
}
