package com.example.ems.reports.export;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportExportHistoryRepository extends JpaRepository<ReportExportHistory, Long> {
    Page<ReportExportHistory> findByCreatedBy(String createdBy, Pageable pageable);
}
