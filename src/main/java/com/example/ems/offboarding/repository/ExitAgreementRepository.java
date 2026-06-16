package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitAgreementRepository extends JpaRepository<ExitAgreement, Long> {
    List<ExitAgreement> findByOffboardingId(Long offboardingId);
}
