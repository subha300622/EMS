package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppraisalHistoryRepository extends JpaRepository<AppraisalHistory, Long> {
    List<AppraisalHistory> findByAppraisalIdOrderByChangedAtAsc(Long appraisalId);
}
