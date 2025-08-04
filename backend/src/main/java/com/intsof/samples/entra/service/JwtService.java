package com.intsof.samples.entra.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(subject)
                    .issuer(issuer)
                    .expirationTime(new Date(System.currentTimeMillis() + refreshExpiration))
                    .claim("type", "refresh")
                    .build();
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate refresh token", e);
        }
    }

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
}