package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MyAssetReturnRequestRepository extends JpaRepository<MyAssetReturnRequest, Long> {
    Optional<MyAssetReturnRequest> findByAssetId(Long assetId);
    List<MyAssetReturnRequest> findByEmployeeId(Long employeeId);

    @Query("SELECT r FROM MyAssetReturnRequest r WHERE r.employee.id IN :teamMemberIds AND (:status IS NULL OR r.status = :status)")
    Page<MyAssetReturnRequest> findByEmployeeIdsAndStatus(
        @Param("teamMemberIds") List<Long> teamMemberIds,
        @Param("status") String status,
        Pageable pageable
    );

    @Query("SELECT COUNT(r) FROM MyAssetReturnRequest r WHERE r.employee.id IN :teamMemberIds AND r.status = 'PENDING_IT_VERIFICATION'")
    long countPendingReturnsByTeamMemberIds(@Param("teamMemberIds") List<Long> teamMemberIds);
}
