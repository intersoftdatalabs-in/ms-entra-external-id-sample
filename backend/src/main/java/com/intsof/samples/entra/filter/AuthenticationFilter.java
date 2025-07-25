package com.intsof.samples.entra.filter;

import com.intsof.samples.entra.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component("authenticationFilter") // Register this filter as a Spring bean
public class AuthenticationFilter implements Filter {

    String frontEndLoginUrl = "http://localhost:4200/login";

    @Autowired
    private UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();
        if (path.equals("/login") && req.getMethod().equalsIgnoreCase("POST")) {
            // Handle login directly in filter
            String username = req.getHeader("X-Username");
            String password = req.getHeader("X-Password");
             boolean isAuthenticated = userService.authenticate(username, password);
            if (username != null && password != null && isAuthenticated) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.setContentType("application/json");
                res.getWriter().write("{\"username\": \"" + username + "\"}");
            } else {
                // Return 401 with error JSON to prevent browser basic auth dialog
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\": \"Invalid credentials\"}");
            }
            return;
        }

        // For other endpoints, require authentication
        String username = req.getHeader("X-Username");
        String password = req.getHeader("X-Password");
        if (username != null && password != null && userService.authenticate(username, password)) {
            chain.doFilter(request, response);
        } else {
            // Return 200 with error JSON for unauthenticated API requests
            res.setStatus(HttpServletResponse.SC_OK);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\": \"Unauthorized\"}");
            return;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
// Java filter for authentication