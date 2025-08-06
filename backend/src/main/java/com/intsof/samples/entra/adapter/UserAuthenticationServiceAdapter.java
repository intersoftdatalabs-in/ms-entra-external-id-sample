package com.intsof.samples.entra.adapter;

import com.intsof.samples.entra.service.UserService;
import com.intsof.samples.security.spi.UserAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Bridges the UserService from the application layer into the security-module
 * via the {@link UserAuthenticationService} abstraction.
 */
@Component
public class UserAuthenticationServiceAdapter implements UserAuthenticationService {

    private final UserService userService;

    @Autowired
    public UserAuthenticationServiceAdapter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean authenticate(String email, String password) {
        return userService.authenticate(email, password);
    }
}
