package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MyAssetReturnRequestRepository extends JpaRepository<MyAssetReturnRequest, Long> {
    Optional<MyAssetReturnRequest> findByAssetId(Long assetId);
    List<MyAssetReturnRequest> findByEmployeeId(Long employeeId);
}
