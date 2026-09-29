package com.example.ems.training.repository;

import com.example.ems.training.entity.Training;
import com.example.ems.training.entity.TrainingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainingRepository extends JpaRepository<Training, Long> {
    List<Training> findByOrganizationId(Long organizationId);
    List<Training> findByOrganizationIdAndStatus(Long organizationId, TrainingStatus status);
    List<Training> findByOrganizationIdAndTrainerId(Long organizationId, Long trainerId);

    @Query("SELECT t FROM Training t WHERE t.organizationId = :orgId AND (:status IS NULL OR t.status = :status) AND (:category IS NULL OR t.category = :category) AND (:trainerId IS NULL OR t.trainerId = :trainerId)")
    List<Training> findWithFilters(@Param("orgId") Long orgId, @Param("status") TrainingStatus status, @Param("category") String category, @Param("trainerId") Long trainerId);

    @Query("SELECT t FROM Training t WHERE t.organizationId = :orgId AND t.startDateTime >= :startDate AND t.endDateTime <= :endDate")
    List<Training> findByCalendarRange(@Param("orgId") Long orgId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long countByOrganizationIdAndStatus(Long organizationId, TrainingStatus status);
    long countByOrganizationId(Long organizationId);
}
