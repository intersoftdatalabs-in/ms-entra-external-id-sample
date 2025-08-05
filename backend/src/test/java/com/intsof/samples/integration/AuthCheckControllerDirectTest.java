package com.intsof.samples.integration;

import com.intsof.samples.entra.controller.AuthCheckController;
import com.intsof.samples.entra.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify AuthCheckController works correctly
 */
public class AuthCheckControllerDirectTest {
    
    private AuthCheckController authCheckController;
    
    @Mock
    private UserService userService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        authCheckController = new AuthCheckController();
        ReflectionTestUtils.setField(authCheckController, "userService", userService);
        ReflectionTestUtils.setField(authCheckController, "ssoEnabledDomains", List.of("gmail.com", "microsoft.com"));
    }
    
    @Test
    public void testCheckAuthMethodWithQueryParam() {
        // Test with email as query parameter (simulating frontend request)
        ResponseEntity<?> response = authCheckController.checkAuthMethod("test@gmail.com", null);
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("test@gmail.com", responseBody.get("email"));
        assertEquals(true, responseBody.get("requiresSSO"));
        assertEquals("SSO", responseBody.get("authMethod"));
    }
    
    @Test
    public void testCheckAuthMethodWithRequestBody() {
        // Test with email in request body
        Map<String, String> requestBody = Map.of("email", "test@example.com");
        ResponseEntity<?> response = authCheckController.checkAuthMethod(null, requestBody);
        
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("test@example.com", responseBody.get("email"));
        assertEquals(false, responseBody.get("requiresSSO")); // example.com is not in SSO domains
        assertEquals("PASSWORD", responseBody.get("authMethod"));
    }
    
    @Test
    public void testCheckAuthMethodWithMissingEmail() {
        // Test with no email provided
        ResponseEntity<?> response = authCheckController.checkAuthMethod(null, null);
        
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("Email is required", responseBody.get("error"));
    }
}
