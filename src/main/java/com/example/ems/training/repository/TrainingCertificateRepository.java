package com.example.ems.training.repository;

import com.example.ems.training.entity.TrainingCertificate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingCertificateRepository extends JpaRepository<TrainingCertificate, Long> {
    Optional<TrainingCertificate> findByEnrollmentId(Long enrollmentId);
    Optional<TrainingCertificate> findByCertificateNumber(String certificateNumber);
}
