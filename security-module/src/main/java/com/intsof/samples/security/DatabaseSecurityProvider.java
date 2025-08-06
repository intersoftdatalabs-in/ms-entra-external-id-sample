package com.intsof.samples.security;

import com.intsof.samples.security.spi.UserAuthenticationService;
import org.springframework.stereotype.Component;

/**
 * Security provider that validates credentials against a user store provided by
 * the application via {@link UserAuthenticationService}.
 */
@Component
public class DatabaseSecurityProvider implements ISecurityProvider {

    private final UserAuthenticationService userAuthenticationService;

    public DatabaseSecurityProvider(UserAuthenticationService userAuthenticationService) {
        this.userAuthenticationService = userAuthenticationService;
    }

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        boolean success = userAuthenticationService.authenticate(username, password);
        if (success) {
            return new AuthenticationResult(true, username, "Authentication successful");
        }
        return new AuthenticationResult(false, null, ApplicationConstants.ERROR_INVALID_CREDENTIALS);
    }

    @Override
    public boolean supports(String type) {
        return "DB".equalsIgnoreCase(type);
    }

    @Override
    public void logout(String sessionId) {
        // No-op for simple DB authentication
    }
}
