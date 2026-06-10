package com.example.ems.repository;

import com.example.ems.entity.DmsDocumentShare;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DmsDocumentShareRepository extends JpaRepository<DmsDocumentShare, Long> {
    List<DmsDocumentShare> findBySharedWithId(Long sharedWithId);
    List<DmsDocumentShare> findByDocumentId(Long documentId);
}
