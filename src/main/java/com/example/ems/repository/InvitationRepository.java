package com.example.ems.repository;

import com.example.ems.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInvitationToken(String invitationToken);
    Optional<Invitation> findByEmail(String email);
    boolean existsByEmail(String email);
}
