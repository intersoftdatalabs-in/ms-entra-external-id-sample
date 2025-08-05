package com.intsof.samples.entra.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.intsof.samples.entra.model.RefreshTokenFamily;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    @Value("${jwt.issuer}")
    private String issuer;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    // Refresh token families for tracking token theft
    private final Map<String, RefreshTokenFamily> refreshTokenFamilies = new ConcurrentHashMap<>();

    // Public constructor for testing purposes
    public JwtService() {}

    public String generateToken(String subject, List<String> roles, Map<String, Object> claims) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .expirationTime(new Date(System.currentTimeMillis() + expiration))
                    .claim("roles", roles);
            if (claims != null) {
                claims.forEach(builder::claim);
            }
            JWTClaimsSet claimsSet = builder.build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());
            return signedJWT.verify(verifier) &&
                    signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JWT token", e);
        }
    }

    public String generateRefreshToken(String subject) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());
            String familyId = UUID.randomUUID().toString();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .expirationTime(new Date(System.currentTimeMillis() + refreshExpiration))
                    .claim("type", "refresh")
                    .claim("familyId", familyId)
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            String token = signedJWT.serialize();
            
            // Create new refresh token family
            RefreshTokenFamily family = new RefreshTokenFamily(familyId, subject, token);
            refreshTokenFamilies.put(familyId, family);
            
            return token;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate refresh token", e);
        }
    }
    
    /**
     * Enhanced refresh token rotation with family tracking
     */
    public RefreshTokenResult refreshTokenWithRotation(String currentRefreshToken) {
        try {
            // Validate the current refresh token
            if (!validateToken(currentRefreshToken)) {
                return new RefreshTokenResult(false, "Invalid refresh token", null, null, null);
            }
            
            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(currentRefreshToken)) {
                return new RefreshTokenResult(false, "Token has been revoked", null, null, null);
            }
            
            JWTClaimsSet claimsSet = parseToken(currentRefreshToken);
            String subject = claimsSet.getSubject();
            String familyId = (String) claimsSet.getClaim("familyId");
            
            if (familyId == null) {
                // Legacy token without family - create new family
                return createNewTokenFamily(subject);
            }
            
            RefreshTokenFamily family = refreshTokenFamilies.get(familyId);
            if (family == null) {
                // Family not found - possible token theft or expired family
                return new RefreshTokenResult(false, "Token family not found", null, null, "TOKEN_FAMILY_NOT_FOUND");
            }
            
            // Check for token reuse (potential theft)
            if (family.isTokenReuse(currentRefreshToken)) {
                // Mark family as compromised and blacklist all tokens
                family.markAsCompromised();
                Date expirationTime = new Date(System.currentTimeMillis() + refreshExpiration);
                tokenBlacklistService.blacklistTokenFamily(family.getAllTokens(), expirationTime);
                refreshTokenFamilies.remove(familyId);
                
                return new RefreshTokenResult(false, "Token reuse detected - possible theft", null, null, "TOKEN_THEFT_DETECTED");
            }
            
            // Validate that this is the current token in the family
            if (!family.isValidForRefresh(currentRefreshToken)) {
                family.markAsCompromised();
                Date expirationTime = new Date(System.currentTimeMillis() + refreshExpiration);
                tokenBlacklistService.blacklistTokenFamily(family.getAllTokens(), expirationTime);
                refreshTokenFamilies.remove(familyId);
                
                return new RefreshTokenResult(false, "Invalid token for refresh", null, null, "INVALID_TOKEN_FOR_REFRESH");
            }
            
            // Generate new tokens
            List<String> roles = extractRoles(currentRefreshToken);
            String newAccessToken = generateToken(subject, roles, null);
            String newRefreshToken = generateNewRefreshTokenInFamily(family, subject);
            
            // Blacklist the old refresh token
            Date oldTokenExpiration = claimsSet.getExpirationTime();
            if (oldTokenExpiration != null) {
                tokenBlacklistService.blacklistToken(currentRefreshToken, oldTokenExpiration);
            }
            
            return new RefreshTokenResult(true, "Token refresh successful", newAccessToken, newRefreshToken, null);
            
        } catch (Exception e) {
            return new RefreshTokenResult(false, "Token refresh failed: " + e.getMessage(), null, null, "REFRESH_ERROR");
        }
    }
    
    /**
     * Generate a new refresh token within an existing family
     */
    private String generateNewRefreshTokenInFamily(RefreshTokenFamily family, String subject) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .expirationTime(new Date(System.currentTimeMillis() + refreshExpiration))
                    .claim("type", "refresh")
                    .claim("familyId", family.getFamilyId())
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            String newToken = signedJWT.serialize();
            
            // Rotate token in family
            family.rotateToken(newToken);
            
            return newToken;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate new refresh token in family", e);
        }
    }
    
    /**
     * Create a new token family for legacy tokens
     */
    private RefreshTokenResult createNewTokenFamily(String subject) {
        try {
            String newAccessToken = generateToken(subject, null, null);
            String newRefreshToken = generateRefreshToken(subject);
            
            return new RefreshTokenResult(true, "New token family created", newAccessToken, newRefreshToken, null);
        } catch (Exception e) {
            return new RefreshTokenResult(false, "Failed to create new token family", null, null, "FAMILY_CREATION_ERROR");
        }
    }
    
    /**
     * Invalidate all tokens in a family (for logout)
     */
    public void invalidateTokenFamily(String refreshToken) {
        try {
            JWTClaimsSet claimsSet = parseToken(refreshToken);
            String familyId = (String) claimsSet.getClaim("familyId");
            
            if (familyId != null) {
                RefreshTokenFamily family = refreshTokenFamilies.get(familyId);
                if (family != null) {
                    // Blacklist all tokens in the family
                    Date expirationTime = new Date(System.currentTimeMillis() + refreshExpiration);
                    tokenBlacklistService.blacklistTokenFamily(family.getAllTokens(), expirationTime);
                    refreshTokenFamilies.remove(familyId);
                }
            }
            
            // Also blacklist the specific token if not in a family
            Date tokenExpiration = claimsSet.getExpirationTime();
            if (tokenExpiration != null) {
                tokenBlacklistService.blacklistToken(refreshToken, tokenExpiration);
            }
            
        } catch (Exception e) {
            // Log error but don't fail - just blacklist the specific token
            Date expirationTime = new Date(System.currentTimeMillis() + refreshExpiration);
            tokenBlacklistService.blacklistToken(refreshToken, expirationTime);
        }
    }
    
    /**
     * Enhanced token validation that checks blacklist
     */
    public boolean validateTokenWithBlacklist(String token) {
        if (!validateToken(token)) {
            return false;
        }
        
        return !tokenBlacklistService.isTokenBlacklisted(token);
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            JWTClaimsSet claimsSet = parseToken(token);
            Object rolesObj = claimsSet.getClaim("roles");
            if (rolesObj instanceof List<?>) {
                return (List<String>) rolesObj;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get refresh token family information for monitoring
     */
    public RefreshTokenFamily getTokenFamily(String refreshToken) {
        try {
            JWTClaimsSet claimsSet = parseToken(refreshToken);
            String familyId = (String) claimsSet.getClaim("familyId");
            
            if (familyId != null) {
                return refreshTokenFamilies.get(familyId);
            }
            
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get statistics for monitoring
     */
    public JwtServiceStats getStats() {
        return new JwtServiceStats(
            refreshTokenFamilies.size(),
            tokenBlacklistService.getBlacklistedTokenCount()
        );
    }
    
    /**
     * Result class for refresh token operations
     */
    public static class RefreshTokenResult {
        private final boolean success;
        private final String message;
        private final String accessToken;
        private final String refreshToken;
        private final String errorCode;
        
        public RefreshTokenResult(boolean success, String message, String accessToken, String refreshToken, String errorCode) {
            this.success = success;
            this.message = message;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.errorCode = errorCode;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public String getErrorCode() { return errorCode; }
        
        @Override
        public String toString() {
            return "RefreshTokenResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", errorCode='" + errorCode + '\'' +
                    '}';
        }
    }
    
    /**
     * Statistics class for monitoring
     */
    public static class JwtServiceStats {
        private final int activeTokenFamilies;
        private final int blacklistedTokens;
        
        public JwtServiceStats(int activeTokenFamilies, int blacklistedTokens) {
            this.activeTokenFamilies = activeTokenFamilies;
            this.blacklistedTokens = blacklistedTokens;
        }
        
        public int getActiveTokenFamilies() { return activeTokenFamilies; }
        public int getBlacklistedTokens() { return blacklistedTokens; }
        
        @Override
        public String toString() {
            return "JwtServiceStats{" +
                    "activeTokenFamilies=" + activeTokenFamilies +
                    ", blacklistedTokens=" + blacklistedTokens +
                    '}';
        }
    }
}