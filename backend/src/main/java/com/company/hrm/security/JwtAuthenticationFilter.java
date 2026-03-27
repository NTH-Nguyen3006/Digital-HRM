package com.company.hrm.security;

import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.common.constant.SessionStatus;
import com.company.hrm.common.constant.UserStatus;
import com.company.hrm.module.auth.entity.SecAuthSession;
import com.company.hrm.module.user.entity.SecUserAccount;
import com.company.hrm.module.auth.repository.SecAuthSessionRepository;
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

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, SecAuthSessionRepository authSessionRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.authSessionRepository = authSessionRepository;
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
                        SecurityUserPrincipal principal = buildPrincipal(claims, sessionId);
                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                principal,
                                null,
                                principal.getAuthorities()
                        );
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

    @SuppressWarnings("unchecked")
    private SecurityUserPrincipal buildPrincipal(Claims claims, UUID sessionId) {
        List<String> permissions = claims.get("permissions", List.class);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        RoleCode roleCode = RoleCode.valueOf(claims.get("roleCode", String.class));

        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode.name()));
        if (permissions != null) {
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        }

        return new SecurityUserPrincipal(
                UUID.fromString(claims.getSubject()),
                sessionId,
                claims.get("username", String.class),
                claims.get("email", String.class),
                roleCode,
                permissions == null ? List.of() : permissions,
                authorities
        );
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7);
    }
}
