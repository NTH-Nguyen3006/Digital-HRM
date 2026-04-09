package com.company.hrm.security;

import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.constant.SessionStatus;
import com.company.hrm.common.constant.UserStatus;
import com.company.hrm.config.AppProperties;
import com.company.hrm.module.auth.entity.SecAuthSession;
import com.company.hrm.module.role.repository.SecRolePermissionRepository;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.auth.repository.SecAuthSessionRepository;
import com.company.hrm.module.user.entity.SecUserRole;
import com.company.hrm.module.user.repository.SecUserRoleRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SecAuthSessionRepository authSessionRepository;
    private final SecUserRoleRepository userRoleRepository;
    private final SecRolePermissionRepository rolePermissionRepository;
    private final AppProperties appProperties;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            @Lazy SecAuthSessionRepository authSessionRepository,
            @Lazy SecUserRoleRepository userRoleRepository,
            @Lazy SecRolePermissionRepository rolePermissionRepository,
            AppProperties appProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authSessionRepository = authSessionRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveBearerToken(request);
        if (token != null && jwtTokenProvider.isValid(token)) {
            Claims claims = jwtTokenProvider.parseClaims(token);
            UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));

            authSessionRepository.findByAuthSessionId(sessionId)
                    .filter(session -> isSessionValid(session, session.getUser()))
                    .ifPresent(session -> {
                        SecurityUserPrincipal principal = buildPrincipal(session, claims, sessionId);
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    });
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSessionValid(SecAuthSession session, SecUserAccount user) {
        if (session.getStatus() != SessionStatus.ACTIVE) {
            return false;
        }
        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            authSessionRepository.save(session);
            return false;
        }
        return user.getStatus() == UserStatus.ACTIVE && !user.isDeleted();
    }

    private SecurityUserPrincipal buildPrincipal(SecAuthSession session, Claims claims, UUID sessionId) {
        SecUserAccount user = session.getUser();
        SecUserRole activeRole = userRoleRepository.findActivePrimaryRole(user.getUserId(), LocalDateTime.now())
                .orElse(null);
        RoleCode roleCode = activeRole == null
                ? RoleCode.valueOf(claims.get("roleCode", String.class))
                : activeRole.getRole().getRoleCode();
        List<String> permissions = activeRole == null
                ? List.of()
                : rolePermissionRepository.findAllowedPermissionCodes(activeRole.getRole().getRoleId());
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode.name()));
        if (permissions != null) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }

        return new SecurityUserPrincipal(
                user.getUserId(),
                sessionId,
                user.getUsername(),
                user.getEmail(),
                roleCode,
                permissions == null ? List.of() : permissions,
                authorities);
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        if (request.getCookies() == null) {
            return null;
        }

        String accessTokenCookieName = appProperties.getAuth().getAccessTokenCookieName();
        for (var cookie : request.getCookies()) {
            if (accessTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
