package com.intsof.samples.entra.adapter;

import com.intsof.samples.entra.service.EntraIdService;
import com.intsof.samples.security.spi.ExternalIdTokenService;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter that exposes {@link EntraIdService} functionality through the
 * {@link ExternalIdTokenService} SPI defined in security-module.
 */
@Component
public class ExternalIdTokenServiceAdapter implements ExternalIdTokenService {

    private final EntraIdService entraIdService;

    @Autowired
    public ExternalIdTokenServiceAdapter(EntraIdService entraIdService) {
        this.entraIdService = entraIdService;
    }

    @Override
    public CompletableFuture<IAuthenticationResult> acquireTokenByAuthorizationCode(String authorizationCode, String redirectUri, Set<String> scopes) {
        return entraIdService.acquireTokenByAuthorizationCode(authorizationCode, redirectUri, scopes);
    }

    @Override
    public ExternalUserProfile getUserProfile(String accessToken) {
        EntraIdService.EntraUserProfile p = entraIdService.getUserProfile(accessToken);
        if (p == null) return null;
        return new ExternalUserProfile(p.getEmail(), p.getRoles());
    }

    @Override
    public ExternalTokenValidationResult validateToken(String token) {
        EntraIdService.EntraTokenValidationResult v = entraIdService.validateToken(token);
        ExternalUserProfile profile = v.getUserProfile() != null ? new ExternalUserProfile(v.getUserProfile().getEmail(), v.getUserProfile().getRoles()) : null;
        return new ExternalTokenValidationResult(v.isValid(), v.getMessage(), profile);
    }
}
