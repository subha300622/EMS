package com.example.ems.security.provider;

import com.example.ems.security.dto.AuthAuthenticationToken;
import com.example.ems.security.dto.AuthPrincipal;
import com.example.ems.security.service.AuthenticationDecisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AuthenticationDecisionService authenticationDecisionService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof AuthAuthenticationToken)) {
            return null;
        }

        AuthAuthenticationToken token = (AuthAuthenticationToken) authentication;
        if (token.isAuthenticated()) {
            return token;
        }

        String rawToken = (String) token.getCredentials();
        if (rawToken == null) {
            throw new BadCredentialsException("No token credentials provided");
        }

        // Delegate authentication logic to AuthenticationDecisionService
        AuthPrincipal principal = authenticationDecisionService.authenticateToken(rawToken);

        // Map role to standard Spring Security authority format
        String roleWithPrefix = principal.getRole().startsWith("ROLE_") ? principal.getRole() : "ROLE_" + principal.getRole();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleWithPrefix);

        AuthAuthenticationToken authenticatedToken = new AuthAuthenticationToken(principal, List.of(authority));
        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AuthAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
