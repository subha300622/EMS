package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalTimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppraisalTimelineEventRepository extends JpaRepository<AppraisalTimelineEvent, Long> {
    List<AppraisalTimelineEvent> findByAppraisalIdOrderByTimestampAsc(Long appraisalId);
}
