package com.intsof.samples.security;

import com.intsof.samples.entra.constants.ApplicationConstants;
import com.intsof.samples.entra.service.UserService;

public class DatabaseSecurityProvider implements ISecurityProvider {

    private final UserService userService;

    public DatabaseSecurityProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        boolean success = userService.authenticate(username, password);
        if (success) {
            return new AuthenticationResult(true, username, "Authentication successful");
        } else {
            return new AuthenticationResult(false, null, ApplicationConstants.ERROR_INVALID_CREDENTIALS);
        }
    }

    @Override
    public boolean supports(String type) {
        return "DB".equalsIgnoreCase(type);
    }

    @Override
    public void logout(String sessionId) {
    }
}