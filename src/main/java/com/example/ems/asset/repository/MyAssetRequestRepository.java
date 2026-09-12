package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAssetRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MyAssetRequestRepository extends JpaRepository<MyAssetRequest, Long> {
    List<MyAssetRequest> findByEmployeeIdOrderByRequestedAtDesc(Long employeeId);

    @Query("SELECT r FROM MyAssetRequest r WHERE r.employee.id IN :teamMemberIds AND (:status IS NULL OR r.status = :status)")
    Page<MyAssetRequest> findByEmployeeIdsAndStatus(
        @Param("teamMemberIds") List<Long> teamMemberIds,
        @Param("status") String status,
        Pageable pageable
    );

    @Query("SELECT COUNT(r) FROM MyAssetRequest r WHERE r.employee.id IN :teamMemberIds AND r.status = 'PENDING_MANAGER_APPROVAL'")
    long countPendingRequestsByTeamMemberIds(@Param("teamMemberIds") List<Long> teamMemberIds);
}
