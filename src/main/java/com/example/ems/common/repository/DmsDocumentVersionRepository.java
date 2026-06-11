package com.example.ems.common.repository;

import com.example.ems.common.entity.DmsDocumentVersion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DmsDocumentVersionRepository extends JpaRepository<DmsDocumentVersion, Long> {
    List<DmsDocumentVersion> findByDocumentIdOrderByVersionNumberDesc(Long documentId);
}
