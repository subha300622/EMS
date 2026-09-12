package com.example.ems.employee.repository;

import com.example.ems.employee.entity.TeamAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamAuditLogRepository extends JpaRepository<TeamAuditLog, Long> {

    List<TeamAuditLog> findByTeamIdOrderByTimestampDesc(Long teamId);
}
