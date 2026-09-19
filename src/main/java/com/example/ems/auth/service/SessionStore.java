package com.example.ems.auth.service;

import com.example.ems.auth.entity.UserSession;
import java.util.List;
import java.util.Optional;

public interface SessionStore {
    void save(UserSession session);
    Optional<UserSession> findById(String sessionId);
    Optional<UserSession> findByRefreshToken(String refreshToken);
    List<UserSession> findByUserIdAndIsRevokedFalse(String userId);
    void delete(String sessionId);
    Optional<UserSession> findBySessionIdAndStatusAndSessionVersionAndSessionEpoch(String sessionId, String status, int sessionVersion, long sessionEpoch);
}
