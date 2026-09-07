package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalReviewRepository extends JpaRepository<AppraisalReview, Long> {
    List<AppraisalReview> findByAppraisalIdOrderByStageOrderAsc(Long appraisalId);
    Optional<AppraisalReview> findByAppraisalIdAndStageOrder(Long appraisalId, Integer stageOrder);
    Optional<AppraisalReview> findByAppraisalIdAndReviewerIdAndStageOrder(Long appraisalId, Long reviewerId, Integer stageOrder);
}
