package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalRatingScaleLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalRatingScaleLevelRepository extends JpaRepository<AppraisalRatingScaleLevel, Long> {
    List<AppraisalRatingScaleLevel> findByRatingScaleIdOrderByLevelOrderAsc(Long ratingScaleId);
    List<AppraisalRatingScaleLevel> findByOrganizationIdOrderByLevelOrderAsc(Long organizationId);
    Optional<AppraisalRatingScaleLevel> findByIdAndOrganizationId(Long id, Long organizationId);
}
