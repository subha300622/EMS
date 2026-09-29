package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalRatingScale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalRatingScaleRepository extends JpaRepository<AppraisalRatingScale, Long> {
    List<AppraisalRatingScale> findByOrganizationId(Long organizationId);
    Optional<AppraisalRatingScale> findByOrganizationIdAndActiveTrue(Long organizationId);
    Optional<AppraisalRatingScale> findByIdAndOrganizationId(Long id, Long organizationId);
}
