package com.example.ems.settings.repository;

import com.example.ems.settings.entity.DataExportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DataExportRequestRepository extends JpaRepository<DataExportRequest, Long> {
    Optional<DataExportRequest> findByRequestIdAndUserEmail(String requestId, String userEmail);
}
