package com.example.ems.recruitment.repository;

import com.example.ems.recruitment.entity.BackgroundVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BackgroundVerificationRepository extends JpaRepository<BackgroundVerification, Long> {
    List<BackgroundVerification> findByCandidateId(Long candidateId);
}
