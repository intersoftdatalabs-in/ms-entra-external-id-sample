package com.intsof.samples.entra.service;

import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;
    private final String secret = "0123456789abcdef0123456789abcdef";
    private final long expiration = 3600000;
    private final long refreshExpiration = 86400000;
    private final String issuer = "ms-entra-external-id-sample";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);
        ReflectionTestUtils.setField(jwtService, "issuer", issuer);
    }

    @Test
    void testGenerateAndValidateToken() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtService.generateToken("testuser", roles, null);
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testTokenClaimsExtraction() throws Exception {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        Map<String, Object> claims = new HashMap<>();
        claims.put("custom", "value");
        String token = jwtService.generateToken("testuser", roles, claims);
        JWTClaimsSet claimsSet = jwtService.parseToken(token);
        assertEquals("testuser", claimsSet.getSubject());
        assertEquals(issuer, claimsSet.getIssuer());
        assertEquals(roles, claimsSet.getClaim("roles"));
        assertEquals("value", claimsSet.getClaim("custom"));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        String refreshToken = jwtService.generateRefreshToken("testuser");
        assertNotNull(refreshToken);
        assertTrue(jwtService.validateToken(refreshToken));
    }

    @Test
    void testExtractRoles() {
        List<String> roles = Arrays.asList("USER", "ADMIN");
        String token = jwtService.generateToken("testuser", roles, null);
        List<String> extractedRoles = jwtService.extractRoles(token);
        assertEquals(roles, extractedRoles);
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.value"));
    }
}
