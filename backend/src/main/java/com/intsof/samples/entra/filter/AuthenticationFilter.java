package com.intsof.samples.entra.filter;

import com.intsof.samples.entra.constants.ApplicationConstants;
import com.intsof.samples.entra.service.JwtService;
import com.intsof.samples.entra.service.RateLimitingService;
import com.intsof.samples.entra.service.AuditLoggingService;
import com.intsof.samples.entra.service.TokenBlacklistService;
import com.intsof.samples.entra.dto.TokenResponse;
import com.intsof.samples.security.AuthenticationResult;
import com.intsof.samples.security.DatabaseSecurityProvider;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import com.intsof.samples.security.ISecurityProvider;
import com.intsof.samples.security.SecurityManager;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("authenticationFilter")
public class AuthenticationFilter implements Filter {
    private final JwtService jwtService;
    private final RateLimitingService rateLimitingService;
    private final AuditLoggingService auditLoggingService;
    private final TokenBlacklistService tokenBlacklistService;

    // Legacy support for unit tests that inject this via reflection
    @Autowired(required = false)
    private com.intsof.samples.entra.service.UserService userService;

    // Constructor for testability and DI
    @Autowired
    public AuthenticationFilter(JwtService jwtService, RateLimitingService rateLimitingService, 
                               AuditLoggingService auditLoggingService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.rateLimitingService = rateLimitingService;
        this.auditLoggingService = auditLoggingService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    private static final String LOGIN_PATH = "/login";
    private static final String LOGOUT_PATH = "/logout";
    
    /**
     * Check if a path is whitelisted and doesn't require authentication
     */
    private boolean isWhitelistedPath(String path) {
        return LOGIN_PATH.equals(path) || 
               "/refresh".equals(path) || 
               LOGOUT_PATH.equals(path) ||
               "/auth/check-method".equals(path) ||
               "/auth/entra/authorization-url".equals(path) ||
               "/auth/entra/callback".equals(path) ||
               "/api/sso/config".equals(path) ||
               "/auth/entra/validate".equals(path);
    }

    private SecurityManager securityManager;

    @Autowired
    private DatabaseSecurityProvider dbProvider;

    @Autowired
    private EntraExternalIdSSOProvider ssoProvider;

    @Value("${sso.enabled-domains}")
    private List<String> ssoEnabledDomains;

    @Override
    public void init(FilterConfig filterConfig) {
        ISecurityProvider dbProvider = this.dbProvider;
        ISecurityProvider ssoProvider = this.ssoProvider;

        this.securityManager = new SecurityManager(dbProvider);

        if (ssoEnabledDomains != null && !ssoEnabledDomains.isEmpty()) {
            ssoEnabledDomains.stream()
                    .map(String::trim)
                    .filter(domain -> !domain.isEmpty())
                    .forEach(domain -> this.securityManager.registerProvider(domain.toLowerCase(), ssoProvider));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setContentType(ApplicationConstants.CONTENT_TYPE_JSON);
        String path = req.getRequestURI();
        String method = req.getMethod();
        String ipAddress = getClientIpAddress(req);

        // JWT validation for protected routes - skip authentication endpoints
        if (!isWhitelistedPath(path)) {
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Enhanced validation with blacklist check
                if (jwtService.validateTokenWithBlacklist(token)) {
                    chain.doFilter(request, response);
                    return;
                } else {
                    Map<String, Object> auditData = new HashMap<>();
                    auditData.put("reason", "invalid_or_expired_token");
                    auditData.put("path", path);
                    auditLoggingService.logAuthEvent("TOKEN_VALIDATION_FAILED", null, ipAddress, auditData);
                    
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.getWriter().write("{\"error\": \"Invalid or expired token\"}");
                    return;
                }
            } else {
                Map<String, Object> auditData = new HashMap<>();
                auditData.put("reason", "missing_authorization_header");
                auditData.put("path", path);
                auditLoggingService.logAuthEvent("UNAUTHORIZED_ACCESS_ATTEMPT", null, ipAddress, auditData);
                
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\": \"Missing Authorization header\"}");
                return;
            }
        }

        // Handle login
        if (LOGIN_PATH.equals(path) && "POST".equalsIgnoreCase(method)) {
            String email = req.getHeader("X-Email");
            String password = req.getHeader("X-Password");
            handleLogin(email, password, ipAddress, res);
            return;
        }

        // Handle token refresh
        if ("/refresh".equals(path) && "POST".equalsIgnoreCase(method)) {
            String refreshToken = req.getHeader("X-Refresh-Token");
            handleRefresh(refreshToken, ipAddress, res);
            return;
        }

        // Handle logout
        if (LOGOUT_PATH.equals(path) && "POST".equalsIgnoreCase(method)) {
            String authHeader = req.getHeader("Authorization");
            handleLogout(authHeader, ipAddress, res);
            return;
        }

        // Allow logout to pass through for GET requests (legacy support)
        if (LOGOUT_PATH.equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        // Allow whitelisted paths to pass through to controllers
        if (isWhitelistedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("{\"error\": \"" + ApplicationConstants.ERROR_UNAUTHORIZED + "\"}");
    }

    /**
     * Enhanced login handling with rate limiting and audit logging
     */
    private void handleLogin(String email, String password, String ipAddress, HttpServletResponse res) throws IOException {
        if (email == null) {
            auditLoggingService.logFailedAuth(null, ipAddress, "missing_email_header");
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"error\": \"" + ApplicationConstants.ERROR_EMAIL_HEADER_MISSING + "\"}");
            return;
        }
        
        // Check rate limiting
        if (!rateLimitingService.isAllowed(ipAddress, email)) {
            long waitTime = rateLimitingService.getTimeUntilNextAttempt(ipAddress, email);
            
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("waitTime", waitTime);
            auditData.put("remainingAttempts", rateLimitingService.getRemainingAttempts(ipAddress));
            auditLoggingService.logRateLimitExceeded(email, ipAddress, "LOGIN_RATE_LIMIT");
            
            res.setStatus(429); // HTTP 429 Too Many Requests
            res.getWriter().write("{\"error\": \"Too many login attempts. Try again in " + waitTime + " seconds\", \"retryAfter\": " + waitTime + "}");
            return;
        }
        
        AuthenticationResult result = securityManager.authenticate(email, password);
        if (result.isSuccess()) {
            // Reset rate limiting on successful authentication
            rateLimitingService.resetCounters(ipAddress, email);
            
            // Generate JWT tokens
            String accessToken = jwtService.generateToken(email, result.getRoles(), null);
            String refreshToken = jwtService.generateRefreshToken(email);
            TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken, 3600, email, result.getRoles());
            
            // Audit successful login
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("roles", result.getRoles());
            auditData.put("authMethod", "PASSWORD");
            auditLoggingService.logSuccessfulAuth(email, ipAddress, "PASSWORD", auditData);
            
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(toJson(tokenResponse));
        } else {
            // Check if SSO redirect is required
            if (result.getMessage() != null && result.getMessage().startsWith("SSO_REDIRECT_REQUIRED")) {
                // User needs to authenticate via SSO OAuth flow
                Map<String, Object> auditData = new HashMap<>();
                auditData.put("reason", "sso_redirect_required");
                auditData.put("authMethod", "SSO");
                auditLoggingService.logAuthEvent("SSO_REDIRECT_REQUIRED", email, ipAddress, auditData);
                
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Keep 401 for backward compatibility
                res.getWriter().write("{\"error\": \"SSO_REDIRECT_REQUIRED\", \"requiresSSO\": true, \"authMethod\": \"SSO\", \"authorizationUrl\": \"/auth/entra/authorization-url\", \"message\": \"This email domain requires SSO authentication. Please use the authorization URL to authenticate.\"}");
                return;
            }
            
            // Record failed attempt for rate limiting
            rateLimitingService.recordFailedAttempt(ipAddress, email);
            
            // Audit failed login
            auditLoggingService.logFailedAuth(email, ipAddress, "invalid_credentials");
            
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"" + ApplicationConstants.ERROR_INVALID_CREDENTIALS + "\"}");
        }
    }

    /**
     * Enhanced refresh handling with token rotation and family tracking
     */
    private void handleRefresh(String refreshToken, String ipAddress, HttpServletResponse res) throws IOException {
        if (refreshToken == null) {
            auditLoggingService.logTokenRefresh(null, ipAddress, false, 
                Map.of("reason", "missing_refresh_token"));
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"Missing refresh token\"}");
            return;
        }
        
        // Use enhanced refresh with rotation
        JwtService.RefreshTokenResult refreshResult = jwtService.refreshTokenWithRotation(refreshToken);
        
        if (refreshResult.isSuccess()) {
            // Extract username from old token for audit
            String username = null;
            try {
                username = jwtService.parseToken(refreshToken).getSubject();
            } catch (Exception e) {
                // Token parsing failed, but we still have the new tokens
            }
            
            TokenResponse tokenResponse = new TokenResponse(
                refreshResult.getAccessToken(), 
                refreshResult.getRefreshToken(), 
                3600, 
                username, 
                null
            );
            
            // Audit successful refresh
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("tokenRotated", true);
            auditLoggingService.logTokenRefresh(username, ipAddress, true, auditData);
            
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(toJson(tokenResponse));
        } else {
            // Handle different error scenarios
            String errorCode = refreshResult.getErrorCode();
            String username = null;
            
            try {
                username = jwtService.parseToken(refreshToken).getSubject();
            } catch (Exception e) {
                // Token parsing failed
            }
            
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("errorCode", errorCode);
            auditData.put("reason", refreshResult.getMessage());
            
            // Log security threats
            if ("TOKEN_THEFT_DETECTED".equals(errorCode) || "TOKEN_FAMILY_NOT_FOUND".equals(errorCode)) {
                auditLoggingService.logSecurityThreat("TOKEN_THEFT", username, ipAddress, 
                    "Refresh token reuse detected - possible token theft");
            }
            
            auditLoggingService.logTokenRefresh(username, ipAddress, false, auditData);
            
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"" + refreshResult.getMessage() + "\"}");
        }
    }
    
    /**
     * Enhanced logout handling with token invalidation
     */
    private void handleLogout(String authHeader, String ipAddress, HttpServletResponse res) throws IOException {
        String username = null;
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                username = jwtService.parseToken(accessToken).getSubject();
                
                // Invalidate the access token
                jwtService.parseToken(accessToken); // This will help us get expiration
                tokenBlacklistService.blacklistToken(accessToken, new java.util.Date(System.currentTimeMillis() + 3600000));
            }
            
            // Also check for refresh token to invalidate the entire family
            // In a real implementation, you might need to track active refresh tokens per user
            
            // Audit logout
            auditLoggingService.logLogout(username, ipAddress);
            
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\"message\": \"Logout successful\"}");
            
        } catch (Exception e) {
            auditLoggingService.logLogout(username, ipAddress, 
                Map.of("error", "token_parsing_failed"));
            
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\"message\": \"Logout completed\"}");
        }
    }
    
    /**
     * Get client IP address from request, handling proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String toJson(TokenResponse tokenResponse) {
        // Simple manual JSON serialization for demo
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"accessToken\": \"").append(tokenResponse.getAccessToken()).append("\",");
        sb.append("\"refreshToken\": \"").append(tokenResponse.getRefreshToken()).append("\",");
        sb.append("\"tokenType\": \"").append(tokenResponse.getTokenType()).append("\",");
        sb.append("\"expiresIn\": ").append(tokenResponse.getExpiresIn()).append(",");
        sb.append("\"username\": \"").append(tokenResponse.getUsername()).append("\"");
        if (tokenResponse.getRoles() != null) {
            sb.append(",\"roles\": [");
            for (int i = 0; i < tokenResponse.getRoles().size(); i++) {
                sb.append("\"").append(tokenResponse.getRoles().get(i)).append("\"");
                if (i < tokenResponse.getRoles().size() - 1) sb.append(",");
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void destroy() {
    }
}
