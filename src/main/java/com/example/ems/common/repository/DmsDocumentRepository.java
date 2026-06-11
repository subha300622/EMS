package com.example.ems.common.repository;

import com.example.ems.common.entity.DmsDocument;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DmsDocumentRepository extends JpaRepository<DmsDocument, Long> {
    List<DmsDocument> findByOwnerId(Long ownerId);
    List<DmsDocument> findByStatus(String status);
    List<DmsDocument> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
