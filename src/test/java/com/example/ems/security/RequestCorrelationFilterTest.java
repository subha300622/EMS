package com.example.ems.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.UUID;

public class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter correlationFilter = new RequestCorrelationFilter();

    @Test
    public void testUsesExistingCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-ID", "existing-id-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
                assertEquals("existing-id-123", MDC.get("correlationId"));
            }
        };

        correlationFilter.doFilter(request, response, filterChain);

        assertNull(MDC.get("correlationId"));
        assertEquals("existing-id-123", response.getHeader("X-Request-ID"));
    }

    @Test
    public void testGeneratesCorrelationIdIfMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
                String correlationId = MDC.get("correlationId");
                assertNotNull(correlationId);
                assertFalse(correlationId.trim().isEmpty());
                assertDoesNotThrow(() -> UUID.fromString(correlationId));
            }
        };

        correlationFilter.doFilter(request, response, filterChain);

        assertNull(MDC.get("correlationId"));
        String responseHeader = response.getHeader("X-Request-ID");
        assertNotNull(responseHeader);
        assertDoesNotThrow(() -> UUID.fromString(responseHeader));
    }
}
