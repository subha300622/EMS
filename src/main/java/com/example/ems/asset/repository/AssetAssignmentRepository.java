package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetAssignment;
import com.example.ems.asset.entity.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Long> {

    Optional<AssetAssignment> findByAssetIdAndStatus(Long assetId, AssignmentStatus status);

    Optional<AssetAssignment> findByAssetIdAndOrganizationIdAndStatus(Long assetId, Long organizationId, AssignmentStatus status);

    List<AssetAssignment> findByEmployeeIdAndOrganizationIdAndStatus(Long employeeId, Long organizationId, AssignmentStatus status);

    List<AssetAssignment> findByOrganizationId(Long organizationId);

    List<AssetAssignment> findByAssetIdOrderByAssignedDateDesc(Long assetId);

    List<AssetAssignment> findByAssetIdAndOrganizationId(Long assetId, Long organizationId);

    boolean existsByLocationIdAndStatus(Long locationId, AssignmentStatus status);
}
