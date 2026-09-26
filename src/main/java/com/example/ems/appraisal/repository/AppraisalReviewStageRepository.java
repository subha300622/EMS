package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalReviewStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalReviewStageRepository extends JpaRepository<AppraisalReviewStage, Long> {

    List<AppraisalReviewStage> findByOrganizationIdOrderByStageOrderAsc(Long organizationId);

    Optional<AppraisalReviewStage> findByOrganizationIdAndStageOrder(Long organizationId, Integer stageOrder);

    void deleteByOrganizationId(Long organizationId);
}
