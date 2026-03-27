package com.company.hrm.security;

import com.company.hrm.common.constant.RoleCode;
import com.company.hrm.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final AppProperties appProperties;

    public JwtTokenProvider(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generateAccessToken(
            UUID userId,
            UUID sessionId,
            String username,
            String email,
            RoleCode roleCode,
            List<String> permissions
    ) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(appProperties.getAuth().getAccessTokenMinutes() * 60);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("sessionId", sessionId.toString())
                .claim("username", username)
                .claim("email", email)
                .claim("roleCode", roleCode.name())
                .claim("permissions", permissions)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(appProperties.getAuth().getJwtSecret().getBytes(StandardCharsets.UTF_8));
    }
}
