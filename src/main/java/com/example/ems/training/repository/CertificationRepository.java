package com.example.ems.training.repository;

import com.example.ems.training.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    List<Certification> findByEmployeeId(Long employeeId);

    Optional<Certification> findByEmployeeIdAndCourseId(Long employeeId, Long courseId);

    boolean existsByCertificateNumber(String certificateNumber);
}
