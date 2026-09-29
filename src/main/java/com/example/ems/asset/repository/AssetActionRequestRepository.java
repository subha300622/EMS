package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetActionRequest;
import com.example.ems.asset.entity.AssetActionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetActionRequestRepository extends JpaRepository<AssetActionRequest, Long> {

    Optional<AssetActionRequest> findByApprovalInstanceId(Long approvalInstanceId);

    Optional<AssetActionRequest> findByAssetIdAndStatus(Long assetId, AssetActionStatus status);

    boolean existsByAssetIdAndStatus(Long assetId, AssetActionStatus status);

    List<AssetActionRequest> findByAssetIdAndOrganizationIdOrderByCreatedAtDesc(Long assetId, Long organizationId);
}
