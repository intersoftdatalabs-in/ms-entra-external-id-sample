package com.intsof.samples.entra.filter;

import com.intsof.samples.entra.constants.ApplicationConstants;
import com.intsof.samples.entra.service.UserService;
import com.intsof.samples.entra.service.JwtService;
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
import java.util.List;

@Component("authenticationFilter")
public class AuthenticationFilter implements Filter {
    private final JwtService jwtService;

    // Constructor for testability and DI
    @Autowired
    public AuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private static final String LOGIN_PATH = "/login";
    private static final String LOGOUT_PATH = "/logout";

    private SecurityManager securityManager;

    @Autowired
    private UserService userService;

    @Value("${sso.enabled-domains}")
    private List<String> ssoEnabledDomains;

    @Override
    public void init(FilterConfig filterConfig) {
        ISecurityProvider dbProvider = new DatabaseSecurityProvider(userService);
        ISecurityProvider ssoProvider = new EntraExternalIdSSOProvider();

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

        // JWT validation for protected routes
        if (!LOGIN_PATH.equals(path) && !"/refresh".equals(path)) {
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtService.validateToken(token)) {
                    chain.doFilter(request, response);
                    return;
                } else {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.getWriter().write("{\"error\": \"Invalid or expired token\"}");
                    return;
                }
            } else {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\": \"Missing Authorization header\"}");
                return;
            }
        }

        // Handle login
        if (LOGIN_PATH.equals(path) && "POST".equalsIgnoreCase(method)) {
            String email = req.getHeader("X-Email");
            String password = req.getHeader("X-Password");
            handleLogin(email, password, res);
            return;
        }

        // Handle token refresh
        if ("/refresh".equals(path) && "POST".equalsIgnoreCase(method)) {
            String refreshToken = req.getHeader("X-Refresh-Token");
            handleRefresh(refreshToken, res);
            return;
        }

        // Allow logout to pass through
        if (LOGOUT_PATH.equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write("{\"error\": \"" + ApplicationConstants.ERROR_UNAUTHORIZED + "\"}");
    }

    private void handleLogin(String email, String password, HttpServletResponse res) throws IOException {
        if (email == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"error\": \"" + ApplicationConstants.ERROR_EMAIL_HEADER_MISSING + "\"}");
            return;
        }
        AuthenticationResult result = securityManager.authenticate(email, password);
        if (result.isSuccess()) {
            // Generate JWT tokens
            String accessToken = jwtService.generateToken(email, result.getRoles(), null);
            String refreshToken = jwtService.generateRefreshToken(email);
            TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken, 3600, email, result.getRoles());
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(toJson(tokenResponse));
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"" + ApplicationConstants.ERROR_INVALID_CREDENTIALS + "\"}");
        }
    }

    private void handleRefresh(String refreshToken, HttpServletResponse res) throws IOException {
        if (refreshToken == null || !jwtService.validateToken(refreshToken)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"Invalid or expired refresh token\"}");
            return;
        }
        String email = jwtService.parseToken(refreshToken).getSubject();
        // For demo, roles are not refreshed. In production, fetch latest roles.
        String newAccessToken = jwtService.generateToken(email, null, null);
        TokenResponse tokenResponse = new TokenResponse(newAccessToken, refreshToken, 3600, email, null);
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write(toJson(tokenResponse));
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
