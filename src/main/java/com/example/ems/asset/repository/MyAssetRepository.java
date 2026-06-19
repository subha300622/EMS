package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetRepository extends JpaRepository<MyAsset, Long> {

    @Query("SELECT a FROM MyAsset a WHERE a.assignedTo.id = :employeeId " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:category IS NULL OR a.category = :category) " +
           "AND (:condition IS NULL OR a.condition = :condition)")
    Page<MyAsset> findByFilters(
        @Param("employeeId") Long employeeId,
        @Param("status") String status,
        @Param("category") String category,
        @Param("condition") String condition,
        Pageable pageable
    );

    List<MyAsset> findByAssignedToId(Long employeeId);

    List<MyAsset> findByAssignedToIdAndStatus(Long employeeId, String status);

    java.util.Optional<MyAsset> findByAssetCode(String assetCode);
}
