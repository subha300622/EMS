package com.example.ems.auth.repository;

import com.example.ems.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    Optional<UserSession> findByRefreshToken(String refreshToken);
    List<UserSession> findByUserIdAndIsRevokedFalse(String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserSession> findBySessionIdAndStatusAndSessionVersionAndSessionEpoch(String sessionId, String status, int sessionVersion, long sessionEpoch);
}
