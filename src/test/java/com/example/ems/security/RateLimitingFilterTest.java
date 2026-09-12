package com.example.ems.security;

import com.example.ems.auth.service.SafeRedisService;
import com.example.ems.security.service.JwtService;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RateLimitingFilterTest {

    @Mock
    private SafeRedisService safeRedisService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUnderLimitPassesThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(safeRedisService.increment("rate:limit:192.168.1.1")).thenReturn(50L);

        rateLimitingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testFirstRequestSetsExpiration() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(safeRedisService.increment("rate:limit:192.168.1.1")).thenReturn(1L);

        rateLimitingFilter.doFilter(request, response, filterChain);

        verify(safeRedisService).expire(eq("rate:limit:192.168.1.1"), any(Duration.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testOverLimitReturns429() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(safeRedisService.increment("rate:limit:192.168.1.1")).thenReturn(101L);

        rateLimitingFilter.doFilter(request, response, filterChain);

        verifyNoInteractions(filterChain);
        assertEquals(429, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains("Too many requests"));
    }

    @Test
    public void testIdentifiesUserByJwtEmail() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer mock-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(jwtService.validateAccessToken("mock-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("mock-token")).thenReturn("user@company.com");
        when(safeRedisService.increment("rate:limit:user@company.com")).thenReturn(5L);

        rateLimitingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(safeRedisService).increment("rate:limit:user@company.com");
    }

    @Test
    public void testFallbackToXForwardedFor() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.195");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(safeRedisService.increment("rate:limit:203.0.113.195")).thenReturn(5L);

        rateLimitingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(safeRedisService).increment("rate:limit:203.0.113.195");
    }

    @Test
    public void testRedisFailureAllowsThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(safeRedisService.increment(anyString())).thenReturn(null);

        rateLimitingFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }
}
