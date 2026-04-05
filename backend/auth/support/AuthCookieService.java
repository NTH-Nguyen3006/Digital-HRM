package com.company.hrm.module.auth.support;

import com.company.hrm.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AuthCookieService {

    private final AppProperties appProperties;

    public AuthCookieService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void writeAuthenticationCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addCookie(
                response,
                appProperties.getAuth().getAccessTokenCookieName(),
                accessToken,
                Duration.ofMinutes(appProperties.getAuth().getAccessTokenMinutes()),
                appProperties.getAuth().getAccessTokenCookiePath()
        );
        addCookie(
                response,
                appProperties.getAuth().getRefreshTokenCookieName(),
                refreshToken,
                Duration.ofDays(appProperties.getAuth().getRefreshTokenDays()),
                appProperties.getAuth().getRefreshTokenCookiePath()
        );
    }

    public void clearAuthenticationCookies(HttpServletResponse response) {
        clearCookie(
                response,
                appProperties.getAuth().getAccessTokenCookieName(),
                appProperties.getAuth().getAccessTokenCookiePath()
        );
        clearCookie(
                response,
                appProperties.getAuth().getRefreshTokenCookieName(),
                appProperties.getAuth().getRefreshTokenCookiePath()
        );
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return resolveCookieValue(request, appProperties.getAuth().getAccessTokenCookieName());
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return resolveCookieValue(request, appProperties.getAuth().getRefreshTokenCookieName());
    }

    private String resolveCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void addCookie(HttpServletResponse response, String name, String value, Duration maxAge, String path) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(appProperties.getAuth().isCookieSecure())
                .sameSite(appProperties.getAuth().getCookieSameSite())
                .path(path)
                .maxAge(maxAge);

        if (appProperties.getAuth().getCookieDomain() != null && !appProperties.getAuth().getCookieDomain().isBlank()) {
            builder.domain(appProperties.getAuth().getCookieDomain().trim());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(appProperties.getAuth().isCookieSecure())
                .sameSite(appProperties.getAuth().getCookieSameSite())
                .path(path)
                .maxAge(Duration.ZERO);

        if (appProperties.getAuth().getCookieDomain() != null && !appProperties.getAuth().getCookieDomain().isBlank()) {
            builder.domain(appProperties.getAuth().getCookieDomain().trim());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, builder.build().toString());
    }
}
