package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingLibraryResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrainingLibraryResourceRepository extends JpaRepository<TrainingLibraryResource, Long> {
    List<TrainingLibraryResource> findByOrganizationId(Long organizationId);

    @Query("SELECT r FROM TrainingLibraryResource r WHERE r.organizationId = :orgId AND (:category IS NULL OR r.category = :category) AND (:technology IS NULL OR r.technology = :technology) AND (:trainerId IS NULL OR r.trainerId = :trainerId)")
    List<TrainingLibraryResource> findWithFilters(@Param("orgId") Long orgId, @Param("category") String category, @Param("technology") String technology, @Param("trainerId") Long trainerId);
}
