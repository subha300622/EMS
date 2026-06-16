package com.example.ems.offboarding.repository;

import com.example.ems.offboarding.entity.ExitDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExitDocumentRepository extends JpaRepository<ExitDocument, Long> {
    List<ExitDocument> findByOffboardingId(Long offboardingId);
}
