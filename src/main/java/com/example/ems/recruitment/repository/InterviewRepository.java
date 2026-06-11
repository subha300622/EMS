package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.Interview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByStatus(String status);
    List<Interview> findByCandidateId(Long candidateId);
    List<Interview> findByJobId(Long jobId);
}
