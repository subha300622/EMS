package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetMaintenanceRepository extends JpaRepository<MyAssetMaintenance, Long> {
    List<MyAssetMaintenance> findByAssetIdOrderByStartDateDesc(Long assetId);
}
