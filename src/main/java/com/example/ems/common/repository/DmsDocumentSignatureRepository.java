package com.example.ems.common.repository;

import com.example.ems.common.entity.DmsDocumentSignature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DmsDocumentSignatureRepository extends JpaRepository<DmsDocumentSignature, Long> {
    List<DmsDocumentSignature> findByRequestedFromId(Long requestedFromId);
    List<DmsDocumentSignature> findByDocumentId(Long documentId);
    List<DmsDocumentSignature> findByStatus(String status);
}
