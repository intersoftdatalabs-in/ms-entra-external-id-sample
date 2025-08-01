package com.intsof.samples.entra.filter;

import com.intsof.samples.entra.constants.ApplicationConstants;
import com.intsof.samples.entra.service.UserService;
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
        String email = req.getHeader("X-Email");
        String password = req.getHeader("X-Password");

        if (LOGOUT_PATH.equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (LOGIN_PATH.equals(path) && "POST".equalsIgnoreCase(req.getMethod())) {
            handleLogin(email, password, res);
            return;
        }

        if (email != null) {
            AuthenticationResult result = securityManager.authenticate(email, password);
            if (result.isSuccess()) {
                chain.doFilter(request, response);
                return;
            }
        }

        res.setStatus(HttpServletResponse.SC_OK);
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
            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write("{\"email\": \"" + email + "\"}");
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\": \"" + ApplicationConstants.ERROR_INVALID_CREDENTIALS + "\"}");
        }
    }

    @Override
    public void destroy() {
    }
}
