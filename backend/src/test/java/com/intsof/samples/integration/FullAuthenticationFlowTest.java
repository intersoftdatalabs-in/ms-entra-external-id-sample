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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for complete authentication flow
 */
public class FullAuthenticationFlowTest {
    
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
    public void testSSOUserGetsRedirectResponse() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setRequestURI("/login");
        request.addHeader("X-Email", "test@gmail.com");
        request.addHeader("X-Password", "password");
        
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        
        // Should get unauthorized response with SSO redirect required
        assertEquals(401, response.getStatus());
        String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("SSO_REDIRECT_REQUIRED"));
    }
    
    @Test
    public void testDatabaseUserSuccessfulLogin() throws ServletException, IOException {
        // Mock successful database authentication
        when(userService.authenticate(anyString(), anyString())).thenReturn(true);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setRequestURI("/login");
        request.addHeader("X-Email", "test@example.com");
        request.addHeader("X-Password", "correct-password");
        
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        
        // Should get successful response with tokens
        assertEquals(200, response.getStatus());
        String responseBody = response.getContentAsString();
        assertTrue(responseBody.contains("accessToken"));
        assertTrue(responseBody.contains("refreshToken"));
    }
    
    @Test
    public void testDatabaseUserFailedLogin() throws ServletException, IOException {
        // Mock failed database authentication
        when(userService.authenticate(anyString(), anyString())).thenReturn(false);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setMethod("POST");
        request.setRequestURI("/login");
        request.addHeader("X-Email", "test@example.com");
        request.addHeader("X-Password", "wrong-password");
        
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        
        // Should get unauthorized response without SSO redirect
        assertEquals(401, response.getStatus());
        String responseBody = response.getContentAsString();
        assertFalse(responseBody.contains("SSO_REDIRECT_REQUIRED"));
        assertTrue(responseBody.contains("Invalid credentials"));
    }
}
