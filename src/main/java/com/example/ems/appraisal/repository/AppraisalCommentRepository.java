package com.example.ems.appraisal.repository;

import com.example.ems.appraisal.entity.AppraisalComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppraisalCommentRepository extends JpaRepository<AppraisalComment, Long> {
    List<AppraisalComment> findByAppraisalIdOrderByCreatedAtAsc(Long appraisalId);
}
