package com.example.ems.employee.repository;

import com.example.ems.employee.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamIdAndStatus(Long teamId, String status);

    List<TeamMember> findByTeamId(Long teamId);

    Optional<TeamMember> findByTeamIdAndEmployeeIdAndStatus(Long teamId, Long employeeId, String status);

    Optional<TeamMember> findByTeamIdAndEmployeeId(Long teamId, Long employeeId);

    boolean existsByTeamIdAndEmployeeIdAndStatus(Long teamId, Long employeeId, String status);

    long countByTeamIdAndStatus(Long teamId, String status);

    Optional<TeamMember> findByTeamIdAndIsTeamLeadTrueAndStatus(Long teamId, String status);
}
