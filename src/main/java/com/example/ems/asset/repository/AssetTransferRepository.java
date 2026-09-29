package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetTransferRepository extends JpaRepository<AssetTransfer, Long> {
    List<AssetTransfer> findByOrganizationIdAndAssetIdOrderByTransferredAtDesc(Long organizationId, Long assetId);
    List<AssetTransfer> findByOrganizationIdOrderByTransferredAtDesc(Long organizationId);
}
