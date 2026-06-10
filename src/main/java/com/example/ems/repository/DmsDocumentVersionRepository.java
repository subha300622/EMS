package com.example.ems.repository;

import com.example.ems.entity.DmsDocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DmsDocumentVersionRepository extends JpaRepository<DmsDocumentVersion, Long> {
    List<DmsDocumentVersion> findByDocumentIdOrderByVersionNumberDesc(Long documentId);
}
