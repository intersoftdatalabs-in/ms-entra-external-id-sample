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
            // Handle login directly in filter
            String email = req.getHeader("X-Email");
            String password = req.getHeader("X-Password");
            if (email != null && password != null && userService.authenticate(email, password)) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.setContentType("application/json");
                res.getWriter().write("{\"email\": \"" + email + "\"}");
            } else {
                // Return 401 with error JSON to prevent browser basic auth dialog
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Invalid credentials\"}");
            }
            return;
        }

        // For other endpoints, require authentication
        String email = req.getHeader("X-Email");
        String password = req.getHeader("X-Password");
        if (email != null && password != null && userService.authenticate(email, password)) {
            chain.doFilter(request, response);
        } else {
            // Return 200 with error JSON for unauthenticated API requests
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }
    }

    /* TODO: Fix me
     - domains should be loaded from a config file like sso-enabled-domains
     - that config needs to include any of the IDP specific configuration that we need to talk to entra.
     */
    @Override   
    public void init(FilterConfig filterConfig) throws ServletException {
        ISecurityProvider dbProvider = new DatabaseSecurityProvider();
        ISecurityProvider ssoProvider = new EntraExternalIdSSOProvider();

        this.securityManager = new SecurityManager(dbProvider);
        this.securityManager.registerProvider("sso-company.com", ssoProvider);
        this.securityManager.registerProvider("partner.org", ssoProvider);



    }

    @Override
    public void destroy() {}
}
// Java filter for authentication