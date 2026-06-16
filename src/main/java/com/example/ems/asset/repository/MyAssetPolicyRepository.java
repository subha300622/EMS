package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyAssetPolicyRepository extends JpaRepository<MyAssetPolicy, Long> {
}
