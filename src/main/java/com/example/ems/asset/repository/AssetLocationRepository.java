package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetLocationRepository extends JpaRepository<AssetLocation, Long> {

    Optional<AssetLocation> findByIdAndOrganizationId(Long id, Long organizationId);

    List<AssetLocation> findByOrganizationId(Long organizationId);

    List<AssetLocation> findByOrganizationIdAndActiveTrue(Long organizationId);

    boolean existsByOrganizationIdAndLocationCodeIgnoreCase(Long organizationId, String locationCode);

    boolean existsByOrganizationIdAndLocationNameIgnoreCase(Long organizationId, String locationName);

    boolean existsByOrganizationIdAndLocationCodeIgnoreCaseAndIdNot(Long organizationId, String locationCode, Long id);

    boolean existsByOrganizationIdAndLocationNameIgnoreCaseAndIdNot(Long organizationId, String locationName, Long id);
}
