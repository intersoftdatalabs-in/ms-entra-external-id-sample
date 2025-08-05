package com.intsof.samples.integration;

import com.intsof.samples.entra.filter.AuthenticationFilter;
import com.intsof.samples.entra.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify authentication filter whitelisting works correctly
 */
public class AuthenticationFilterWhitelistTest {
    
    private AuthenticationFilter filter;
    
    @Mock
    private UserService userService;
    
    private JwtService jwtService;
    private RateLimitingService rateLimitingService;
    private AuditLoggingService auditLoggingService;
    private TokenBlacklistService tokenBlacklistService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Set up services
        tokenBlacklistService = new TokenBlacklistService();
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "issuer", "test-issuer");
        ReflectionTestUtils.setField(jwtService, "tokenBlacklistService", tokenBlacklistService);
        
        rateLimitingService = new RateLimitingService();
        ReflectionTestUtils.setField(rateLimitingService, "maxAttempts", 5);
        ReflectionTestUtils.setField(rateLimitingService, "windowMinutes", 1);
        ReflectionTestUtils.setField(rateLimitingService, "rateLimitingEnabled", true);
        
        auditLoggingService = new AuditLoggingService();
        
        filter = new AuthenticationFilter(jwtService, rateLimitingService, auditLoggingService, tokenBlacklistService);
        
        // Set SSO enabled domains
        ReflectionTestUtils.setField(filter, "userService", userService);
        ReflectionTestUtils.setField(filter, "ssoEnabledDomains", List.of("gmail.com", "microsoft.com"));
        
        // Initialize the filter
        try {
            filter.init(null);
        } catch (Exception e) {
            fail("Filter initialization failed");
        }
    }
    
    @Test
    public void testAuthCheckMethodIsWhitelisted() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        
        request.setMethod("POST");
        request.setRequestURI("/auth/check-method");
        request.setParameter("email", "test@example.com");
        
        filter.doFilter(request, response, chain);
        
        // Should pass through to the next filter/controller without authentication
        // Since this is a whitelisted path, it should not return 401
        assertNotEquals(401, response.getStatus());
        
        // The chain should have been called (meaning the request was allowed through)
        assertTrue(response.getStatus() != 401, "Request should not require authentication");
    }
    
    @Test
    public void testProtectedEndpointRequiresAuth() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        
        request.setMethod("GET");
        request.setRequestURI("/some-protected-endpoint");
        
        filter.doFilter(request, response, chain);
        
        // Should return 401 since this is not whitelisted and no auth header provided
        assertEquals(401, response.getStatus());
    }
    
    @Test
    public void testLoginEndpointIsWhitelisted() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        
        request.setMethod("POST");
        request.setRequestURI("/login");
        request.addHeader("X-Email", "test@example.com");
        request.addHeader("X-Password", "password");
        
        filter.doFilter(request, response, chain);
        // The filter should allow the request to reach the login handler, which may return 401 for invalid credentials
        // Check that the response contains the expected error message
        String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("Invalid credentials") || response.getStatus() != 401,
            "Login endpoint should be handled by the login handler, not blocked by the filter");
    }
}
