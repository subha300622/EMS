package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetDocumentRepository extends JpaRepository<MyAssetDocument, Long> {
    List<MyAssetDocument> findByAssetIdOrderByUploadedAtDesc(Long assetId);
}
