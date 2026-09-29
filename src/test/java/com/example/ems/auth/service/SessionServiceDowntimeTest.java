package com.example.ems.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SessionServiceDowntimeTest {

    @Mock
    private DatabaseSessionStore databaseSessionStore;

    @Mock
    private RedisSessionCache redisSessionCache;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetSessionByRefreshToken_RedisDown() {
        when(redisSessionCache.findByRefreshToken(any())).thenReturn(Optional.empty());
        when(databaseSessionStore.findByRefreshToken(any())).thenReturn(Optional.empty());

        assertNull(sessionService.getSessionByRefreshToken("some-token"));
    }

    @Test
    public void testRevokeSession_RedisDown() {
        when(databaseSessionStore.findByRefreshToken(any())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> sessionService.revokeSession("some-token"));
    }

    @Test
    public void testGetActiveSessions_RedisDown() {
        when(databaseSessionStore.findByUserIdAndIsRevokedFalse(any())).thenReturn(Collections.emptyList());

        List<SessionService.SessionMetadata> sessions = sessionService.getActiveSessions("EMP001");
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    @Test
    public void testIsSessionActive_RedisDown() {
        when(databaseSessionStore.findById(any())).thenReturn(Optional.empty());

        assertFalse(sessionService.isSessionActive("EMP001", "session-123"));
    }
}
