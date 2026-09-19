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

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.example.ems.auth.service.RoleService roleService;

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

        List<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();

        // Map role to standard Spring Security authority format
        if (principal.getRole() != null && !principal.getRole().isBlank()) {
            String roleWithPrefix = principal.getRole().startsWith("ROLE_") ? principal.getRole() : "ROLE_" + principal.getRole();
            authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
        }

        // Hydrate granular effective permissions for the user
        if (roleService != null && principal.getUserId() != null) {
            try {
                List<String> permissions = roleService.getPermissionsForUserId(principal.getUserId());
                if (permissions != null) {
                    for (String perm : permissions) {
                        if (perm != null && !perm.isBlank()) {
                            authorities.add(new SimpleGrantedAuthority(perm.trim()));
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        AuthAuthenticationToken authenticatedToken = new AuthAuthenticationToken(principal, authorities);
        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AuthAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
