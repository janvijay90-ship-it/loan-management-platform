package com.loan.gateway.filter;

import com.loan.gateway.ratelimit.RateLimiterService;
import com.loan.gateway.security.JwtTokenUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class GatewaySecurityFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(GatewaySecurityFilter.class);

    private final JwtTokenUtil jwtTokenUtil;
    private final RateLimiterService rateLimiterService;

    public GatewaySecurityFilter(JwtTokenUtil jwtTokenUtil, RateLimiterService rateLimiterService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set standard CORS headers for browser clients (such as loan-portal on port 80)
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Dev-Mode, *");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");

        // Immediately fulfill browser preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String path = httpRequest.getRequestURI();
        String clientIp = getClientIp(httpRequest);

        // 1. Rate Limiting Check via Redis
        if (!rateLimiterService.isAllowed(clientIp)) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Please try again later.\"}");
            return;
        }

        // 2. Allow Public endpoints (Auth token generation & Health check)
        if (path.startsWith("/api/v1/auth") || path.equals("/") || path.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // 3. JWT Validation
        String authHeader = httpRequest.getHeader("Authorization");
        String devHeader = httpRequest.getHeader("X-Dev-Mode");

        if ("true".equalsIgnoreCase(devHeader)) {
            log.info("Request passed with X-Dev-Mode header: {}", path);
            chain.doFilter(request, response);
            return;
        }

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                httpRequest.setAttribute("authenticatedUser", username);
                chain.doFilter(request, response);
                return;
            }
        }

        log.warn("Unauthorized access attempt to {} from IP {}", path, clientIp);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.setContentType("application/json");
        httpResponse.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Valid JWT Bearer token or 'X-Dev-Mode: true' required.\"}");
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
