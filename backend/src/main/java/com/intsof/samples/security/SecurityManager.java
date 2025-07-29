package com.intsof.samples.security;

import java.util.HashMap;
import java.util.Map;

/**
 * SecurityManager selects the appropriate SecurityProvider based on email domain.
 */
public class SecurityManager {

    private final Map<String, ISecurityProvider> domainProviderMap = new HashMap<>();
    private final ISecurityProvider defaultProvider;

    public SecurityManager(ISecurityProvider defaultProvider) {
        this.defaultProvider = defaultProvider;
    }

    /**
     * Registers a provider for a specific email domain.
     *
     * @param domain the email domain (e.g., "company.com")
     * @param provider the SecurityProvider implementation
     */
    public void registerProvider(String domain, ISecurityProvider provider) {
        domainProviderMap.put(domain.toLowerCase(), provider);
    }

    /**
     * Authenticates a user by selecting the appropriate provider based on email domain.
     *
     * @param email the user's email
     * @param password the password or token
     * @return the result of the authentication
     */
    public AuthenticationResult authenticate(String email, String password) {
        String domain = extractDomain(email);
        ISecurityProvider provider = domainProviderMap.getOrDefault(domain, defaultProvider);
        return provider.authenticate(email, password);
    }

    /**
     * Extracts the domain from an email address.
     *
     * @param email the email address
     * @return the domain part of the email
     */
    private String extractDomain(String email) {
        int atIndex = email.lastIndexOf('@');
        return (atIndex != -1) ? email.substring(atIndex + 1).toLowerCase() : "";
    }
}
