package com.intsof.samples.entra.filter;

import com.intsof.samples.entra.service.UserService;
import com.intsof.samples.security.AuthenticationResult;
import com.intsof.samples.security.DatabaseSecurityProvider;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import com.intsof.samples.security.ISecurityProvider;
import com.intsof.samples.security.SecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component("authenticationFilter") // Register this filter as a Spring bean
public class AuthenticationFilter implements Filter {

    String frontEndLoginUrl = "http://localhost:4200/login";

    private SecurityManager securityManager;

    @Autowired
    private UserService userService;

    @org.springframework.beans.factory.annotation.Value("${sso.enabled-domains:}")
    private String ssoEnabledDomains;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();

        /* TODO : Implement me
        AuthenticationResult result = securityManager.authenticate("user@sso-company.com", null);
        System.out.println("Auth success: " + result.isSuccess());
        Code below needs to be refactored to use the correct provider / database logic needd moved to the correct provider
        */
        
        if (path.equals("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        if (path.equals("/login") && req.getMethod().equalsIgnoreCase("POST")) {
            // Handle login directly via SecurityManager which chooses correct provider
            String email = req.getHeader("X-Email");
            String password = req.getHeader("X-Password");
            if (email != null) {
                AuthenticationResult result = securityManager.authenticate(email, password);
                if (result.isSuccess()) {
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"email\": \"" + email + "\"}");
                } else {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.getWriter().write("{\"error\": \"Invalid credentials\"}");
                }
            } else {
                res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Email header missing\"}");
            }
            return;
        }

        // For other endpoints, require authentication using SecurityManager
        String email = req.getHeader("X-Email");
        String password = req.getHeader("X-Password");
        if (email != null) {
            AuthenticationResult result = securityManager.authenticate(email, password);
            if (result.isSuccess()) {
                chain.doFilter(request, response);
                return;
            }
        }
        // Return 200 with error JSON for unauthenticated API requests
        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\": \"Unauthorized\"}");
        return;
    }

    /* TODO: Fix me
     - domains should be loaded from a config file like sso-enabled-domains
     - that config needs to include any of the IDP specific configuration that we need to talk to entra.
     */
    @Override
    public void init(FilterConfig filterConfig) {
        ISecurityProvider dbProvider = new DatabaseSecurityProvider(userService);
        ISecurityProvider ssoProvider = new EntraExternalIdSSOProvider();

        this.securityManager = new SecurityManager(dbProvider);

        // Register SSO provider for each configured domain
        if (ssoEnabledDomains != null && !ssoEnabledDomains.isBlank()) {
            for (String domain : ssoEnabledDomains.split(",")) {
                String trimmed = domain.trim();
                if (!trimmed.isEmpty()) {
                    this.securityManager.registerProvider(trimmed, ssoProvider);
                }
            }
        }
    }

    @Override
    public void destroy() {}
}
// Java filter for authentication