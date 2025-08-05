package com.intsof.samples.entra.controller;

import com.intsof.samples.security.SecurityManager;
import com.intsof.samples.entra.service.UserService;
import com.intsof.samples.security.DatabaseSecurityProvider;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling authentication method checks
 */
@RestController
@RequestMapping("/auth")
public class AuthCheckController {

    @Autowired
    private UserService userService;

    @Value("${sso.enabled-domains}")
    private List<String> ssoEnabledDomains;

    /**
     * Check if user's domain requires SSO authentication
     */
    @PostMapping("/check-method")
    public ResponseEntity<?> checkAuthMethod(@RequestParam("email") String email) {
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }

        // Create a temporary SecurityManager to check domain requirements
        DatabaseSecurityProvider dbProvider = new DatabaseSecurityProvider(userService);
        EntraExternalIdSSOProvider ssoProvider = new EntraExternalIdSSOProvider();
        SecurityManager securityManager = new SecurityManager(dbProvider);

        if (ssoEnabledDomains != null && !ssoEnabledDomains.isEmpty()) {
            ssoEnabledDomains.stream()
                    .map(String::trim)
                    .filter(domain -> !domain.isEmpty())
                    .forEach(domain -> securityManager.registerProvider(domain.toLowerCase(), ssoProvider));
        }

        boolean requiresSSO = securityManager.requiresSSO(email);

        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("requiresSSO", requiresSSO);
        response.put("authMethod", requiresSSO ? "SSO" : "PASSWORD");
        
        if (requiresSSO) {
            response.put("authorizationUrl", "/auth/entra/authorization-url");
            response.put("message", "This email domain requires SSO authentication");
        } else {
            response.put("message", "Use password authentication");
        }

        return ResponseEntity.ok(response);
    }
}
