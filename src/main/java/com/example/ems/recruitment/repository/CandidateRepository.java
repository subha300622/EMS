package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.Candidate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    List<Candidate> findByStatus(String status);
    List<Candidate> findByJobId(Long jobId);
}
