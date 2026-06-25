package com.example.ems.security.context;

import com.example.ems.security.dto.AuthAuthenticationToken;
import com.example.ems.security.dto.AuthPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextFacadeImpl implements SecurityContextFacade {

    @Override
    public AuthPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof AuthAuthenticationToken && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof AuthPrincipal) {
                return (AuthPrincipal) principal;
            }
        }
        return null;
    }

    @Override
    public String getSessionId() {
        AuthPrincipal principal = getPrincipal();
        return principal != null ? principal.getSessionId() : null;
    }

    @Override
    public String getUserId() {
        AuthPrincipal principal = getPrincipal();
        return principal != null ? principal.getUserId() : null;
    }

    @Override
    public String getEmail() {
        AuthPrincipal principal = getPrincipal();
        return principal != null ? principal.getEmail() : null;
    }

    @Override
    public boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated();
    }
}
