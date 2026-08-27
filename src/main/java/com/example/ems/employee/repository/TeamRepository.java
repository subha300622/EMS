package com.example.ems.employee.repository;

import com.example.ems.employee.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, JpaSpecificationExecutor<Team> {

    Optional<Team> findByIdAndOrganizationIdAndDeletedFalse(Long id, Long organizationId);

    Optional<Team> findByIdAndDeletedFalse(Long id);

    boolean existsByTeamNameAndOrganizationIdAndDeletedFalse(String teamName, Long organizationId);

    boolean existsByTeamCodeAndOrganizationIdAndDeletedFalse(String teamCode, Long organizationId);

    boolean existsByTeamNameAndOrganizationIdAndIdNotAndDeletedFalse(String teamName, Long organizationId, Long id);

    boolean existsByTeamCodeAndOrganizationIdAndIdNotAndDeletedFalse(String teamCode, Long organizationId, Long id);

    List<Team> findByDepartmentIdAndOrganizationIdAndDeletedFalse(Long departmentId, Long organizationId);
}
