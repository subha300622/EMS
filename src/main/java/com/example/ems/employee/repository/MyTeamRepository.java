package com.example.ems.employee.repository;

import com.example.ems.employee.entity.MyTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MyTeamRepository extends JpaRepository<MyTeam, Long> {
    Optional<MyTeam> findByTeamName(String teamName);
}
