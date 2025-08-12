package com.intsof.samples.entra.controller;

import com.intsof.samples.entra.dto.TokenResponse;
import com.intsof.samples.entra.service.JwtService;
import com.intsof.samples.security.AuthenticationResult;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import com.intsof.samples.entra.dto.LoginRequest;
import com.intsof.samples.entra.service.EntraIdService;
import com.intsof.samples.entra.service.VerifiedAppService;
import com.intsof.samples.entra.service.EntraIdService.EntraTokenValidationResult;
import com.intsof.samples.entra.service.EntraIdService.EntraUserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Controller for handling Microsoft Entra External ID OAuth flow
 */
@RestController
@RequestMapping("/auth/entra")
public class EntraAuthController {
    
    @Autowired
    private EntraExternalIdSSOProvider entraProvider;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private EntraIdService entraIdService;

    @Autowired
    private VerifiedAppService verifiedAppService;
    
    @Value("${sso.registration.azure.client-id}")
    private String clientId;
    
    @Value("${sso.registration.azure.tenant-id}")
    private String tenantId;
    
    @Value("${sso.provider.azure.authorization-uri:https://login.microsoftonline.com/}")
    private String authorizationUri;

    @Value("${sso.registration.azure.redirect-uri}")
    private String redirectUri;

    /**
     * Handle OAuth callback from Entra External ID
     */
    @GetMapping("/callback")
    public ResponseEntity<?> handleOAuthCallback(
            @RequestParam("code") String authorizationCode) {
        
        try {
            AuthenticationResult result = entraProvider.authenticateWithAuthorizationCode(authorizationCode, redirectUri);
            
            if (result.isSuccess()) {
                // Generate application JWT tokens with domain-verified applications
                String userId = result.getUserId();
                String domain = userId.contains("@") ? userId.substring(userId.indexOf('@') + 1).toLowerCase() : "";
                java.util.List<String> apps = verifiedAppService.getVerifiedApplicationsForDomain(domain);
                java.util.Map<String, Object> claims = new java.util.HashMap<>();
                claims.put("applications", apps);
                String accessToken = jwtService.generateToken(userId, result.getRoles(), claims);
                String refreshToken = jwtService.generateRefreshToken(userId);
                
                TokenResponse tokenResponse = new TokenResponse(
                    accessToken,
                    refreshToken,
                    result.getExpiresIn(),
                    userId,
                    result.getRoles()
                );
                tokenResponse.setApplications(apps);
                
                return ResponseEntity.ok(tokenResponse);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", result.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "OAuth callback processing failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Validate an existing Entra ID token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateEntraToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Missing or invalid Authorization header");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            
            String token = authHeader.substring(7);
            AuthenticationResult result = entraProvider.validateEntraToken(token);
            
            if (result.isSuccess()) {
                // Generate application JWT tokens with domain-verified applications
                String userId = result.getUserId();
                String domain = userId.contains("@") ? userId.substring(userId.indexOf('@') + 1).toLowerCase() : "";
                java.util.List<String> apps = verifiedAppService.getVerifiedApplicationsForDomain(domain);
                java.util.Map<String, Object> claims = new java.util.HashMap<>();
                claims.put("applications", apps);
                String accessToken = jwtService.generateToken(userId, result.getRoles(), claims);
                String refreshToken = jwtService.generateRefreshToken(userId);
                
                TokenResponse tokenResponse = new TokenResponse(
                    accessToken,
                    refreshToken,
                    result.getExpiresIn(),
                    userId,
                    result.getRoles()
                );
                tokenResponse.setApplications(apps);
                
                return ResponseEntity.ok(tokenResponse);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", result.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Token validation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Handle credentials-based login
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginWithCredentials(@RequestBody LoginRequest request) {
        EntraTokenValidationResult result = entraIdService.authenticateWithCredentials(request.getUsername(), request.getPassword());
        if (!result.isValid()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", result.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        EntraUserProfile profile = result.getUserProfile();
        String email = profile.getEmail();
        String domain = email.contains("@") ? email.substring(email.indexOf('@') + 1).toLowerCase() : "";
        List<String> apps = verifiedAppService.getVerifiedApplicationsForDomain(domain);
        Map<String, Object> claims = new HashMap<>();
        claims.put("applications", apps);
        String accessToken = jwtService.generateToken(email, profile.getRoles(), claims);
        String refreshToken = jwtService.generateRefreshToken(email);
        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken, 3600, email, profile.getRoles());
        tokenResponse.setApplications(apps);
        return ResponseEntity.ok(tokenResponse);
    }
    
    /**
     * Get Entra ID authorization URL for frontend
     */
    @GetMapping("/authorization-url")
    public ResponseEntity<?> getAuthorizationUrl(
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "state", required = false) String state) {
        
        try {
            // In a real implementation, you would construct the proper authorization URL
            // using MSAL4J or build it manually with the configured endpoints
            String authorizationUrl = buildAuthorizationUrl(redirectUri, state);
            
            Map<String, String> response = new HashMap<>();
            response.put("authorization_url", authorizationUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate authorization URL: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Build authorization URL for Entra External ID
     */
    private String buildAuthorizationUrl(String redirectUri, String state) {
        // Build the authorization URL using actual configuration values
        StringBuilder url = new StringBuilder();
        url.append(authorizationUri);
        if (!authorizationUri.endsWith("/")) {
            url.append("/");
        }
        url.append("?client_id=").append(clientId);
        url.append("&response_type=code");
        url.append("&redirect_uri=").append(redirectUri);
        url.append("&scope=openid%20profile%20email");
        if (state != null && !state.isEmpty()) {
            url.append("&state=").append(state);
        }
        
        return url.toString();
    }
}
