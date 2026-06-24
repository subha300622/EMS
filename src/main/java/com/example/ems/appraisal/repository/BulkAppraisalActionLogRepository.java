package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.BulkAppraisalActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkAppraisalActionLogRepository extends JpaRepository<BulkAppraisalActionLog, Long> {
}
