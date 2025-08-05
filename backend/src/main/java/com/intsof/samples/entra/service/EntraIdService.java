package com.intsof.samples.entra.service;

import com.microsoft.aad.msal4j.*;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for handling Microsoft Entra External ID integration
 * Provides token validation, user profile extraction, and role mapping
 */
@Service
public class EntraIdService {
    
    @Value("${sso.registration.azure.client-id:}")
    private String clientId;
    
    @Value("${sso.registration.azure.tenant-id:}")
    private String tenantId;
    
    @Value("${sso.registration.azure.client-secret:}")
    private String clientSecret;
    
    @Value("${sso.provider.azure.authorization-uri:}")
    private String authorizationUri;
    
    @Value("${sso.provider.token-uri:}")
    private String tokenUri;
    
    @Value("${sso.provider.user-info-uri:}")
    private String userInfoUri;
    
    /**
     * Validate an Entra ID token and extract user information
     */
    public EntraTokenValidationResult validateToken(String token) {
        try {
            JWT jwt = JWTParser.parse(token);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            
            // Validate token expiration
            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                return new EntraTokenValidationResult(false, "Token has expired", null);
            }
            
            // Validate issuer
            String issuer = claimsSet.getIssuer();
            if (issuer == null || !isValidIssuer(issuer)) {
                return new EntraTokenValidationResult(false, "Invalid token issuer", null);
            }
            
            // Validate audience
            List<String> audiences = claimsSet.getAudience();
            if (audiences == null || !audiences.contains(clientId)) {
                return new EntraTokenValidationResult(false, "Invalid token audience", null);
            }
            
            // Extract user profile
            EntraUserProfile userProfile = extractUserProfile(claimsSet);
            
            return new EntraTokenValidationResult(true, "Token validation successful", userProfile);
            
        } catch (Exception e) {
            return new EntraTokenValidationResult(false, "Token validation failed: " + e.getMessage(), null);
        }
    }
    
    /**
     * Exchange authorization code for tokens
     */
    public CompletableFuture<IAuthenticationResult> acquireTokenByAuthorizationCode(
            String authorizationCode, String redirectUri, Set<String> scopes) {
        try {
            ConfidentialClientApplication app = createConfidentialClientApplication();
            
            AuthorizationCodeParameters parameters = AuthorizationCodeParameters
                .builder(authorizationCode, URI.create(redirectUri))
                .scopes(scopes != null ? scopes : Set.of("openid", "profile", "email"))
                .build();
            
            return app.acquireToken(parameters);
            
        } catch (Exception e) {
            CompletableFuture<IAuthenticationResult> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }
    
    /**
     * Get user profile from user info endpoint
     */
    public EntraUserProfile getUserProfile(String accessToken) {
        try {
            // In a real implementation, you would call the user info endpoint
            // For now, we'll extract from the token
            JWT jwt = JWTParser.parse(accessToken);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            return extractUserProfile(claimsSet);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract user profile from JWT claims
     */
    private EntraUserProfile extractUserProfile(JWTClaimsSet claimsSet) {
        try {
            String email = claimsSet.getStringClaim("email");
            if (email == null) {
                email = claimsSet.getStringClaim("preferred_username");
            }
            
            String name = claimsSet.getStringClaim("name");
            String givenName = claimsSet.getStringClaim("given_name");
            String familyName = claimsSet.getStringClaim("family_name");
            String subject = claimsSet.getSubject();
            
            List<String> roles = extractRoles(claimsSet);
            List<String> groups = extractGroups(claimsSet);
            
            return new EntraUserProfile(email, name, givenName, familyName, subject, roles, groups);
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract roles from JWT claims
     */
    private List<String> extractRoles(JWTClaimsSet claimsSet) {
        List<String> roles = new ArrayList<>();
        
        try {
            Object rolesClaim = claimsSet.getClaim("roles");
            if (rolesClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> rolesList = (List<String>) rolesClaim;
                roles.addAll(rolesList);
            }
            
            // Map Entra roles to application roles
            roles = mapEntraRolesToApplicationRoles(roles);
            
            // Default role if no roles found
            if (roles.isEmpty()) {
                roles.add("USER");
            }
            
        } catch (Exception e) {
            roles.add("USER");
        }
        
        return roles;
    }
    
    /**
     * Extract groups from JWT claims
     */
    private List<String> extractGroups(JWTClaimsSet claimsSet) {
        List<String> groups = new ArrayList<>();
        
        try {
            Object groupsClaim = claimsSet.getClaim("groups");
            if (groupsClaim instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> groupsList = (List<String>) groupsClaim;
                groups.addAll(groupsList);
            }
        } catch (Exception e) {
            // Ignore groups extraction errors
        }
        
        return groups;
    }
    
    /**
     * Map Entra ID roles to application-specific roles
     */
    private List<String> mapEntraRolesToApplicationRoles(List<String> entraRoles) {
        List<String> appRoles = new ArrayList<>();
        
        for (String entraRole : entraRoles) {
            switch (entraRole.toLowerCase()) {
                case "admin":
                case "administrator":
                case "global administrator":
                    appRoles.add("ADMIN");
                    break;
                case "user":
                case "member":
                    appRoles.add("USER");
                    break;
                case "manager":
                case "supervisor":
                    appRoles.add("MANAGER");
                    break;
                default:
                    // Keep original role name in uppercase
                    appRoles.add(entraRole.toUpperCase());
                    break;
            }
        }
        
        return appRoles;
    }
    
    /**
     * Create MSAL4J Confidential Client Application
     */
    private ConfidentialClientApplication createConfidentialClientApplication() throws MalformedURLException {
        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            throw new IllegalStateException("Entra ID client configuration is missing");
        }
        
        IClientCredential credential = ClientCredentialFactory.createFromSecret(clientSecret);
        
        return ConfidentialClientApplication
            .builder(clientId, credential)
            .authority("https://login.microsoftonline.com/" + tenantId)
            .build();
    }
    
    /**
     * Validate if the issuer is from a trusted Microsoft endpoint
     */
    private boolean isValidIssuer(String issuer) {
        return issuer.startsWith("https://login.microsoftonline.com/") ||
               issuer.startsWith("https://sts.windows.net/");
    }
    
    /**
     * Result class for token validation
     */
    public static class EntraTokenValidationResult {
        private final boolean valid;
        private final String message;
        private final EntraUserProfile userProfile;
        
        public EntraTokenValidationResult(boolean valid, String message, EntraUserProfile userProfile) {
            this.valid = valid;
            this.message = message;
            this.userProfile = userProfile;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public EntraUserProfile getUserProfile() { return userProfile; }
    }
    
    /**
     * User profile extracted from Entra ID
     */
    public static class EntraUserProfile {
        private final String email;
        private final String name;
        private final String givenName;
        private final String familyName;
        private final String subject;
        private final List<String> roles;
        private final List<String> groups;
        
        public EntraUserProfile(String email, String name, String givenName, String familyName, 
                               String subject, List<String> roles, List<String> groups) {
            this.email = email;
            this.name = name;
            this.givenName = givenName;
            this.familyName = familyName;
            this.subject = subject;
            this.roles = roles != null ? roles : new ArrayList<>();
            this.groups = groups != null ? groups : new ArrayList<>();
        }
        
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getGivenName() { return givenName; }
        public String getFamilyName() { return familyName; }
        public String getSubject() { return subject; }
        public List<String> getRoles() { return roles; }
        public List<String> getGroups() { return groups; }
    }
}
