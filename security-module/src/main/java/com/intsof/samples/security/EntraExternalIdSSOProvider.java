package com.intsof.samples.security;

import com.intsof.samples.security.spi.ExternalIdTokenService;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation that relies on Microsoft Entra External ID. The heavy
 * lifting (token exchange, validation, etc.) is delegated to an application-
 * specific {@link ExternalIdTokenService} implementation supplied by the
 * consuming microservice.
 */
@Component
public class EntraExternalIdSSOProvider implements ISecurityProvider {

    private final ExternalIdTokenService externalIdTokenService;

    @Autowired
    public EntraExternalIdSSOProvider(ExternalIdTokenService externalIdTokenService) {
        this.externalIdTokenService = externalIdTokenService;
    }

    @Override
    public AuthenticationResult authenticate(String username, String password) {
        // Direct password auth not supported for SSO – instruct caller to start OAuth flow
        return new AuthenticationResult(false, null,
                "Direct username/password authentication not supported for Entra External ID");
    }

    /**
     * Complete authentication using an authorization code received from the
     * client-side OIDC flow.
     */
    public AuthenticationResult authenticateWithAuthorizationCode(String authorizationCode, String redirectUri) {
        try {
            CompletableFuture<IAuthenticationResult> future = externalIdTokenService
                    .acquireTokenByAuthorizationCode(authorizationCode, redirectUri, Set.of("openid", "profile", "email"));

            IAuthenticationResult result = future.get();
            if (result != null && result.accessToken() != null) {
                ExternalIdTokenService.ExternalUserProfile profile = externalIdTokenService.getUserProfile(result.accessToken());
                if (profile != null) {
                    return new AuthenticationResult(true, profile.getEmail(), "Authentication successful",
                            result.accessToken(), result.accessToken(),
                            result.expiresOnDate().getTime() / 1000, profile.getRoles());
                }
            }
            return new AuthenticationResult(false, null, "Failed to acquire token or extract user profile");
        } catch (Exception e) {
            return new AuthenticationResult(false, null, "Entra External ID authentication failed: " + e.getMessage());
        }
    }

    /** Validate an already issued token. */
    public AuthenticationResult validateEntraToken(String token) {
        try {
            ExternalIdTokenService.ExternalTokenValidationResult validation = externalIdTokenService.validateToken(token);
            if (validation.isValid() && validation.getUserProfile() != null) {
                ExternalIdTokenService.ExternalUserProfile profile = validation.getUserProfile();
                return new AuthenticationResult(true, profile.getEmail(), "Token validation successful",
                        token, token, 0, profile.getRoles());
            }
            return new AuthenticationResult(false, null, validation.getMessage());
        } catch (Exception e) {
            return new AuthenticationResult(false, null, "Token validation failed: " + e.getMessage());
        }
    }

    /** Convenience helper to check if domain has SSO enabled. */
    public boolean isDomainEnabledForSSO(String email, List<String> enabledDomains) {
        if (email == null || !email.contains("@") || enabledDomains == null) {
            return false;
        }
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return enabledDomains.stream().map(String::toLowerCase).anyMatch(domain::equals);
    }

    @Override
    public boolean supports(String type) {
        return "EntraExternalIdSSO".equalsIgnoreCase(type);
    }

    @Override
    public void logout(String sessionId) {
        // Optional: implement token revocation/logout if necessary.
    }
}
