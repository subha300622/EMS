package com.example.ems.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpResolver {

    private ClientIpResolver() {}

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            String[] ips = xForwardedFor.split(",");
            if (ips.length > 0) {
                String primaryIp = ips[0].trim();
                if (!primaryIp.isBlank() && !"unknown".equalsIgnoreCase(primaryIp)) {
                    return primaryIp;
                }
            }
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isBlank()) {
            return remoteAddr.trim();
        }

        return "0.0.0.0";
    }
}
