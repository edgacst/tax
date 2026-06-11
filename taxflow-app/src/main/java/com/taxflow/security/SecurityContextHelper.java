package com.taxflow.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static Optional<AuthenticatedUser> currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public static AuthenticatedUser requireUser() {
        return currentUser().orElseThrow(() ->
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."));
    }

    public static boolean isTenantAdmin(AuthenticatedUser user) {
        return "TENANT_ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole());
    }
}
