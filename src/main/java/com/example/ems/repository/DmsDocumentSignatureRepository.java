package com.example.ems.repository;

import com.example.ems.entity.DmsDocumentSignature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DmsDocumentSignatureRepository extends JpaRepository<DmsDocumentSignature, Long> {
    List<DmsDocumentSignature> findByRequestedFromId(Long requestedFromId);
    List<DmsDocumentSignature> findByDocumentId(Long documentId);
    List<DmsDocumentSignature> findByStatus(String status);
}
