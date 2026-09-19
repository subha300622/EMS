package com.example.ems.auth.service;

import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DatabaseSessionStore implements SessionStore {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Override
    public void save(UserSession session) {
        userSessionRepository.save(session);
    }

    @Override
    public Optional<UserSession> findById(String sessionId) {
        return userSessionRepository.findById(sessionId);
    }

    @Override
    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        return userSessionRepository.findByRefreshToken(refreshToken);
    }

    @Override
    public List<UserSession> findByUserIdAndIsRevokedFalse(String userId) {
        return userSessionRepository.findByUserIdAndIsRevokedFalse(userId);
    }

    @Override
    public void delete(String sessionId) {
        userSessionRepository.deleteById(sessionId);
    }

    @Override
    public Optional<UserSession> findBySessionIdAndStatusAndSessionVersionAndSessionEpoch(String sessionId, String status, int sessionVersion, long sessionEpoch) {
        return userSessionRepository.findBySessionIdAndStatusAndSessionVersionAndSessionEpoch(sessionId, status, sessionVersion, sessionEpoch);
    }
}
