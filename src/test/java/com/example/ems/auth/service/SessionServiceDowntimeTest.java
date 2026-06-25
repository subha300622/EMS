package com.example.ems.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SessionServiceDowntimeTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testGetSessionByRefreshToken_RedisDown() {
        when(valueOperations.get(any())).thenThrow(new RedisConnectionFailureException("Redis is offline"));

        assertNull(sessionService.getSessionByRefreshToken("some-token"));
    }

    @Test
    public void testRevokeSession_RedisDown() {
        when(valueOperations.get(any())).thenThrow(new RedisConnectionFailureException("Redis is offline"));

        assertDoesNotThrow(() -> sessionService.revokeSession("some-token"));
    }

    @Test
    public void testGetActiveSessions_RedisDown() {
        when(redisTemplate.keys(any())).thenThrow(new RedisConnectionFailureException("Redis is offline"));

        List<SessionService.SessionMetadata> sessions = sessionService.getActiveSessions("EMP001");
        assertNotNull(sessions);
        assertTrue(sessions.isEmpty());
    }

    @Test
    public void testIsSessionActive_RedisDown() {
        when(redisTemplate.hasKey(any())).thenThrow(new RedisConnectionFailureException("Redis is offline"));

        assertTrue(sessionService.isSessionActive("EMP001", "session-123"));
    }
}
