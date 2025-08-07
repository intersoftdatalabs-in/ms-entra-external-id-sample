package com.intsof.samples.security.spi;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Abstraction over operations required by SSO providers that integrate with
 * Microsoft Entra External ID (or other OpenID Connect providers).
 *
 * The concrete implementation lives in the consuming micro-service, allowing
 * security-module to stay implementation agnostic.
 */
public interface ExternalIdTokenService {

    /** Acquire an access token from an authorization code obtained during the OIDC flow. */
    CompletableFuture<IAuthenticationResult> acquireTokenByAuthorizationCode(String authorizationCode,
                                                                              String redirectUri,
                                                                              Set<String> scopes);

    /** Extract the user profile from an already issued access token. */
    ExternalUserProfile getUserProfile(String accessToken);

    /** Validate a raw JWT token and return validation data. */
    ExternalTokenValidationResult validateToken(String token);

    /** Lightweight representation of a user profile. */
    class ExternalUserProfile {
        private final String email;
        private final List<String> roles;

        public ExternalUserProfile(String email, List<String> roles) {
            this.email = email;
            this.roles = roles;
        }

        public String getEmail() { return email; }
        public List<String> getRoles() { return roles; }
    }

    /** Result wrapper for token validation. */
    class ExternalTokenValidationResult {
        private final boolean valid;
        private final String message;
        private final ExternalUserProfile userProfile;

        public ExternalTokenValidationResult(boolean valid, String message, ExternalUserProfile userProfile) {
            this.valid = valid;
            this.message = message;
            this.userProfile = userProfile;
        }
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public ExternalUserProfile getUserProfile() { return userProfile; }
    }
}
