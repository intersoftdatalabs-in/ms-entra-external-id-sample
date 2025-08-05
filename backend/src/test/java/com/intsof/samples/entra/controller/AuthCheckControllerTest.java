package com.intsof.samples.entra.controller;

import com.intsof.samples.entra.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuthCheckController
 */
public class AuthCheckControllerTest {
    
    private AuthCheckController controller;
    
    @Mock
    private UserService userService;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AuthCheckController();
        
        // Inject mocks
        try {
            java.lang.reflect.Field userServiceField = AuthCheckController.class.getDeclaredField("userService");
            userServiceField.setAccessible(true);
            userServiceField.set(controller, userService);
            
            // Set SSO enabled domains for testing
            ReflectionTestUtils.setField(controller, "ssoEnabledDomains", List.of("gmail.com", "microsoft.com"));
        } catch (Exception e) {
            fail("Failed to inject mocks");
        }
    }
    
    @Test
    public void testCheckAuthMethod_SSORequired() {
        ResponseEntity<?> response = controller.checkAuthMethod("test@gmail.com", null);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("test@gmail.com", responseBody.get("email"));
        assertEquals(true, responseBody.get("requiresSSO"));
        assertEquals("SSO", responseBody.get("authMethod"));
        assertEquals("/auth/entra/authorization-url", responseBody.get("authorizationUrl"));
    }
    
    @Test
    public void testCheckAuthMethod_PasswordRequired() {
        ResponseEntity<?> response = controller.checkAuthMethod("test@example.com", null);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("test@example.com", responseBody.get("email"));
        assertEquals(false, responseBody.get("requiresSSO"));
        assertEquals("PASSWORD", responseBody.get("authMethod"));
        assertNull(responseBody.get("authorizationUrl"));
    }
    
    @Test
    public void testCheckAuthMethod_MissingEmail() {
        ResponseEntity<?> response = controller.checkAuthMethod("", null);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("Email is required", responseBody.get("error"));
    }
    
    @Test
    public void testCheckAuthMethod_NullEmail() {
        ResponseEntity<?> response = controller.checkAuthMethod(null, null);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("Email is required", responseBody.get("error"));
    }
    
    @Test
    public void testCheckAuthMethod_MicrosoftDomain() {
        ResponseEntity<?> response = controller.checkAuthMethod("user@microsoft.com", null);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        
        assertEquals("user@microsoft.com", responseBody.get("email"));
        assertEquals(true, responseBody.get("requiresSSO"));
        assertEquals("SSO", responseBody.get("authMethod"));
    }
}
