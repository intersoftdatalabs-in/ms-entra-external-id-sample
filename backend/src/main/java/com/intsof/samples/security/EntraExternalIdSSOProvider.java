package com.intsof.samples.security;

import com.intsof.samples.entra.service.EntraIdService;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Component
public class EntraExternalIdSSOProvider implements ISecurityProvider {
    
    @Autowired
    private EntraIdService entraIdService;

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        // For Entra External ID, we typically don't use username/password
        // Instead, we validate existing tokens or initiate OAuth flow
        return new AuthenticationResult(false, null, 
            "Direct username/password authentication not supported for Entra External ID");
    }
    
    /**
     * Authenticate using an authorization code from the OAuth flow
     */
    public AuthenticationResult authenticateWithAuthorizationCode(String authorizationCode, String redirectUri) {
        try {
            CompletableFuture<IAuthenticationResult> future = entraIdService
                .acquireTokenByAuthorizationCode(authorizationCode, redirectUri, 
                    Set.of("openid", "profile", "email"));
            
            IAuthenticationResult result = future.get();
            
            if (result != null && result.accessToken() != null) {
                // Get user profile from the service
                EntraIdService.EntraUserProfile userProfile = entraIdService.getUserProfile(result.accessToken());
                
                if (userProfile != null) {
                    return new AuthenticationResult(true, userProfile.getEmail(), "Authentication successful", 
                        result.accessToken(), result.accessToken(), // Using access token as refresh token for demo
                        result.expiresOnDate().getTime() / 1000, userProfile.getRoles());
                }
            }
            
            return new AuthenticationResult(false, null, "Failed to acquire token or extract user profile");
            
        } catch (Exception e) {
            return new AuthenticationResult(false, null, 
                "Entra External ID authentication failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate an existing Entra ID token
     */
    public AuthenticationResult validateEntraToken(String token) {
        try {
            EntraIdService.EntraTokenValidationResult validationResult = entraIdService.validateToken(token);
            
            if (validationResult.isValid() && validationResult.getUserProfile() != null) {
                EntraIdService.EntraUserProfile userProfile = validationResult.getUserProfile();
                
                return new AuthenticationResult(true, userProfile.getEmail(), "Token validation successful",
                    token, token, // Using same token as refresh token for demo
                    0, userProfile.getRoles()); // Expiration would be extracted from token
            } else {
                return new AuthenticationResult(false, null, validationResult.getMessage());
            }
            
        } catch (Exception e) {
            return new AuthenticationResult(false, null, 
                "Token validation failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if a user's domain is configured for Entra External ID SSO
     */
    public boolean isDomainEnabledForSSO(String email, List<String> enabledDomains) {
        if (email == null || !email.contains("@") || enabledDomains == null) {
            return false;
        }
        
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return enabledDomains.stream()
            .map(String::toLowerCase)
            .anyMatch(enabledDomain -> enabledDomain.equals(domain));
    }

    @Override
    public boolean supports(String type) {
        return "EntraExternalIdSSO".equalsIgnoreCase(type);
    }

    @Override
    public void logout(String sessionId) {
        // Implement logout logic for Entra External ID SSO here
        // This could involve invalidating tokens or calling Microsoft logout endpoints
    }
    
}
