package com.company.hrm.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestContextUtils {

    private RequestContextUtils() {
    }

    public static String getClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }

        String forwardedFor = getForwardedForRaw();
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String forwarded = request.getHeader("Forwarded");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] segments = forwarded.split(";");
            for (String segment : segments) {
                String trimmed = segment.trim();
                if (trimmed.toLowerCase().startsWith("for=")) {
                    return trimmed.substring(4).replace("\"", "").trim();
                }
            }
        }
        return request.getRemoteAddr();
    }

    public static String getForwardedForRaw() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader("X-Forwarded-For");
    }

    public static String getUserAgent() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    public static String getHeader(String headerName) {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader(headerName);
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }
}
