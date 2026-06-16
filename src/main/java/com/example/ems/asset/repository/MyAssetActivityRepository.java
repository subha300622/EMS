package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetActivityRepository extends JpaRepository<MyAssetActivity, Long> {
    List<MyAssetActivity> findByAssetIdOrderByDateDesc(Long assetId);
}
