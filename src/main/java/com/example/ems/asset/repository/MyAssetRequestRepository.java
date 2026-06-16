package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetRequestRepository extends JpaRepository<MyAssetRequest, Long> {
    List<MyAssetRequest> findByEmployeeIdOrderByRequestedAtDesc(Long employeeId);
}
