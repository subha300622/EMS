package com.example.ems.security;

import com.example.ems.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthorizationHeaderFilterTest {

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthorizationHeaderFilter authorizationHeaderFilter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExcludedPaths() throws ServletException, IOException {
        String[] excludedPaths = {"/api/v1/auth/login", "/swagger-ui/index.html", "/v3/api-docs"};

        for (String path : excludedPaths) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI(path);
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = mock(MockFilterChain.class);

            authorizationHeaderFilter.doFilter(request, response, filterChain);

            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtService);
            reset(filterChain);
        }
    }

    @Test
    public void testNullOrEmptyAuthorizationHeaderInjectsDevToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/employees/dashboard");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        when(jwtService.generateAccessToken("EMP005", "emssuperadmin@gmail.com", "SUPER_ADMIN"))
                .thenReturn("mock-super-admin-token");

        authorizationHeaderFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertNotSame(request, wrappedRequest);
        
        String authHeader = wrappedRequest.getHeader("Authorization");
        assertEquals("Bearer mock-super-admin-token", authHeader);

        Enumeration<String> headers = wrappedRequest.getHeaders("Authorization");
        assertNotNull(headers);
        List<String> headersList = Collections.list(headers);
        assertEquals(1, headersList.size());
        assertEquals("Bearer mock-super-admin-token", headersList.get(0));
    }

    @Test
    public void testValidAuthorizationHeaderPassedCleanly() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/employees/dashboard");
        request.addHeader("Authorization", "Bearer eyJ.mocktoken.xyz");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        authorizationHeaderFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        String authHeader = wrappedRequest.getHeader("Authorization");
        assertEquals("Bearer eyJ.mocktoken.xyz", authHeader);
        verifyNoInteractions(jwtService);
    }

    @Test
    public void testCleanHeaderDuplicateBearerPrefix() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/employees/dashboard");
        request.addHeader("Authorization", "Bearer bearer Bearer eyJ.token.abc");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        authorizationHeaderFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        String authHeader = wrappedRequest.getHeader("Authorization");
        assertEquals("Bearer eyJ.token.abc", authHeader);
        verifyNoInteractions(jwtService);
    }

    @Test
    public void testCleanHeaderNonJwtValue() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/employees/dashboard");
        request.addHeader("Authorization", "   Bearer   someRandomCustomTokenValueWithoutDots   ");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = mock(MockFilterChain.class);

        authorizationHeaderFilter.doFilter(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        String authHeader = wrappedRequest.getHeader("Authorization");
        assertEquals("Bearer   someRandomCustomTokenValueWithoutDots", authHeader);
        verifyNoInteractions(jwtService);
    }
}
