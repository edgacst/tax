package com.taxflow.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authenticated principal stored in the security context.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser implements Authentication {

    private Long userId;
    private Long tenantId;
    private String email;
    private String name;
    private String role;
    private boolean mfaVerified;
    private String sessionId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("tenantId", tenantId);
        details.put("role", role);
        return details;
    }

    @Override
    public Object getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        // no-op
    }

    @Override
    public String getName() {
        return email;
    }
}
