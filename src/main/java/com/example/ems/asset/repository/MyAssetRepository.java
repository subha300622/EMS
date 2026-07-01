package com.example.ems.asset.repository;

import com.example.ems.asset.entity.MyAsset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
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

    @Query("SELECT a FROM MyAsset a WHERE " +
           "(:status IS NULL OR " +
           "  (:status = 'AVAILABLE' AND a.status IN ('UNASSIGNED', 'RETURNED', 'AVAILABLE')) OR " +
           "  (:status <> 'AVAILABLE' AND a.status = :status)" +
           ") AND " +
           "(:category IS NULL OR a.category = :category) AND " +
           "(:department IS NULL OR (a.assignedTo IS NOT NULL AND a.assignedTo.department = :department)) AND " +
           "(:search IS NULL OR " +
           "  LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(a.assetName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(a.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(a.model) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%'))" +
           ")")
    Page<MyAsset> findFiltered(
        @Param("status") String status,
        @Param("category") String category,
        @Param("department") String department,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("SELECT a FROM MyAsset a WHERE a.assignedTo.id IN :teamMemberIds " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:search = '' OR " +
           "  LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(a.assetName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(a.serialNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "  LOWER(a.assignedTo.fullName) LIKE LOWER(CONCAT('%', :search, '%'))" +
           ")")
    Page<MyAsset> findByTeamMemberIdsAndFilters(
        @Param("teamMemberIds") List<Long> teamMemberIds,
        @Param("status") String status,
        @Param("search") String search,
        Pageable pageable
    );

    @Query("SELECT a.category, COUNT(a) FROM MyAsset a WHERE a.assignedTo.id IN :teamMemberIds GROUP BY a.category")
    List<Object[]> countAssetsByCategory(@Param("teamMemberIds") List<Long> teamMemberIds);

    @Query("SELECT COUNT(a) FROM MyAsset a WHERE a.assignedTo.id IN :teamMemberIds AND a.status IN ('ASSIGNED', 'RETURN_REQUESTED')")
    long countTeamAssets(@Param("teamMemberIds") List<Long> teamMemberIds);

    @Query("SELECT COALESCE(SUM(a.currentValue), 0) FROM MyAsset a WHERE a.assignedTo.id IN :teamMemberIds AND a.status IN ('ASSIGNED', 'RETURN_REQUESTED')")
    BigDecimal sumTeamAssetValue(@Param("teamMemberIds") List<Long> teamMemberIds);

    @Query("SELECT COUNT(DISTINCT a.assignedTo.id) FROM MyAsset a WHERE a.assignedTo.id IN :teamMemberIds AND a.status IN ('ASSIGNED', 'RETURN_REQUESTED')")
    long countTeamMembersWithAssets(@Param("teamMemberIds") List<Long> teamMemberIds);
}

