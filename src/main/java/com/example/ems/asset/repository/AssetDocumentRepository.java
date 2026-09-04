package com.example.ems.asset.repository;

import com.example.ems.asset.entity.AssetDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssetDocumentRepository extends JpaRepository<AssetDocument, Long> {
    List<AssetDocument> findByOrganizationIdAndAssetId(Long organizationId, Long assetId);
    Optional<AssetDocument> findByOrganizationIdAndId(Long organizationId, Long id);
}
