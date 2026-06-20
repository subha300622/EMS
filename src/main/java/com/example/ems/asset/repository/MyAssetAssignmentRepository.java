package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetAssignmentRepository extends JpaRepository<MyAssetAssignment, Long> {
    List<MyAssetAssignment> findByAssetIdOrderByAssignedDateDesc(Long assetId);
}
