package com.example.ems.auth.repository;

import com.example.ems.auth.entity.Invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInvitationToken(String invitationToken);
    Optional<Invitation> findByEmail(String email);
    boolean existsByEmail(String email);
}
