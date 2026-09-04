package com.example.ems.security.context;

import com.example.ems.security.dto.AuthPrincipal;

public interface SecurityContextFacade {
    AuthPrincipal getPrincipal();
    String getSessionId();
    String getUserId();
    String getEmail();
    boolean isAuthenticated();
}
