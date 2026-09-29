package com.example.ems.goal.repository;

import com.example.ems.goal.domain.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("enterpriseGoalRepository")
public interface GoalRepository extends JpaRepository<Goal, Long>, JpaSpecificationExecutor<Goal> {

    Optional<Goal> findByIdAndOrganizationIdAndIsDeletedFalse(Long id, Long organizationId);

    Optional<Goal> findByGoalNumberAndOrganizationIdAndIsDeletedFalse(String goalNumber, Long organizationId);

    Page<Goal> findByOrganizationIdAndIsDeletedFalse(Long organizationId, Pageable pageable);

    Page<Goal> findByOrganizationIdAndOwnerIdAndIsDeletedFalse(Long organizationId, Long ownerId, Pageable pageable);

    List<Goal> findByOrganizationIdAndParentGoalIdAndIsDeletedFalse(Long organizationId, Long parentGoalId);

    List<Goal> findByOrganizationIdAndOwnerIdAndIsDeletedFalse(Long organizationId, Long ownerId);

    @Query("SELECT COUNT(g) FROM EnterpriseGoal g WHERE g.organizationId = :orgId AND g.isDeleted = false")
    long countByOrganizationId(@Param("orgId") Long orgId);

    @Query("SELECT COUNT(g) FROM EnterpriseGoal g WHERE g.organizationId = :orgId AND g.status = :status AND g.isDeleted = false")
    long countByOrganizationIdAndStatus(@Param("orgId") Long orgId, @Param("status") String status);

    @Query("SELECT COUNT(g) FROM EnterpriseGoal g WHERE g.organizationId = :orgId AND g.ownerId = :ownerId AND g.isDeleted = false")
    long countByOrganizationIdAndOwnerId(@Param("orgId") Long orgId, @Param("ownerId") Long ownerId);

    @Query("SELECT COUNT(g) FROM EnterpriseGoal g WHERE g.organizationId = :orgId AND g.ownerId = :ownerId AND g.status = :status AND g.isDeleted = false")
    long countByOrganizationIdAndOwnerIdAndStatus(@Param("orgId") Long orgId, @Param("ownerId") Long ownerId, @Param("status") String status);
}
