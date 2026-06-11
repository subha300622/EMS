package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitInterview;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExitInterviewRepository extends JpaRepository<ExitInterview, Long> {
    List<ExitInterview> findByOffboardingId(Long offboardingId);
}
