package com.intsof.samples.security.spi;

/**
 * Abstraction for verifying user credentials. Backends can provide their own
 * implementation (e.g. via database lookup, LDAP, etc.) and register it as a
 * Spring bean so that default provider implementations in security-module can
 * remain decoupled from application-specific services.
 */
public interface UserAuthenticationService {

    /**
     * Validate that the supplied email/password pair is valid.
     *
     * @param email    user email/username
     * @param password user password (plain text or already hashed depending on implementation)
     * @return true if credentials are correct, false otherwise
     */
    boolean authenticate(String email, String password);
}
