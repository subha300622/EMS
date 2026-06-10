package com.example.ems.repository;

import com.example.ems.entity.TrainingCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrainingCertificateRepository extends JpaRepository<TrainingCertificate, Long> {
    Optional<TrainingCertificate> findByEnrollmentId(Long enrollmentId);
    Optional<TrainingCertificate> findByCertificateNumber(String certificateNumber);
}
