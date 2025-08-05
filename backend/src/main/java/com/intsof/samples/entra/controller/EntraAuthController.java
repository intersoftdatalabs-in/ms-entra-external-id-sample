package com.intsof.samples.entra.controller;

import com.intsof.samples.entra.dto.TokenResponse;
import com.intsof.samples.entra.service.JwtService;
import com.intsof.samples.security.AuthenticationResult;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    
    @Value("${sso.registration.azure.client-id}")
    private String clientId;
    
    @Value("${sso.registration.azure.tenant-id}")
    private String tenantId;
    
    @Value("${sso.provider.azure.authorization-uri:https://login.microsoftonline.com/}")
    private String authorizationUri;
    
    /**
     * Handle OAuth callback from Entra External ID
     */
    @PostMapping("/callback")
    public ResponseEntity<?> handleOAuthCallback(
            @RequestParam("code") String authorizationCode,
            @RequestParam("redirect_uri") String redirectUri) {
        
        try {
            AuthenticationResult result = entraProvider.authenticateWithAuthorizationCode(authorizationCode, redirectUri);
            
            if (result.isSuccess()) {
                // Generate application JWT tokens
                String accessToken = jwtService.generateToken(result.getUserId(), result.getRoles(), null);
                String refreshToken = jwtService.generateRefreshToken(result.getUserId());
                
                TokenResponse tokenResponse = new TokenResponse(
                    accessToken, 
                    refreshToken, 
                    3600, 
                    result.getUserId(), 
                    result.getRoles()
                );
                
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
                // Generate application JWT tokens
                String accessToken = jwtService.generateToken(result.getUserId(), result.getRoles(), null);
                String refreshToken = jwtService.generateRefreshToken(result.getUserId());
                
                TokenResponse tokenResponse = new TokenResponse(
                    accessToken, 
                    refreshToken, 
                    3600, 
                    result.getUserId(), 
                    result.getRoles()
                );
                
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
        url.append(tenantId).append("/oauth2/v2.0/authorize");
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
