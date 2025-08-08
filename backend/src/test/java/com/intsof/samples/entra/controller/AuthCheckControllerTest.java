package com.intsof.samples.entra.controller;

import com.intsof.samples.security.DatabaseSecurityProvider;
import com.intsof.samples.security.EntraExternalIdSSOProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.util.*;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthCheckControllerTest {

    private AuthCheckController controller;
    private DatabaseSecurityProvider dbProvider;
    private EntraExternalIdSSOProvider ssoProvider;

    @BeforeEach
    public void setup() {
        dbProvider = mock(DatabaseSecurityProvider.class);
        ssoProvider = mock(EntraExternalIdSSOProvider.class);

        controller = new AuthCheckController();
        ReflectionTestUtils.setField(controller, "dbProvider", dbProvider);
        ReflectionTestUtils.setField(controller, "ssoProvider", ssoProvider);
        ReflectionTestUtils.setField(controller, "ssoEnabledDomains", List.of("example.com", "test.org"));
    }

    @Test
    public void testCheckAuthMethod_withSSODomain() {
        String email = "user@example.com";

        ResponseEntity<?> response = controller.checkAuthMethod(email, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("SSO", body.get("authMethod"));
        assertEquals(true, body.get("requiresSSO"));
        assertEquals("/auth/entra/authorization-url", body.get("authorizationUrl"));
    }

    @Test
    public void testCheckAuthMethod_withNonSSODomain() {
        String email = "user@otherdomain.com";

        ResponseEntity<?> response = controller.checkAuthMethod(email, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("PASSWORD", body.get("authMethod"));
        assertEquals(false, body.get("requiresSSO"));
        assertEquals("Use password authentication", body.get("message"));
    }

    @Test
    public void testCheckAuthMethod_missingEmail() {
        ResponseEntity<?> response = controller.checkAuthMethod(null, null);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("Email is required", body.get("error"));
    }

    @Test
    public void testCheckAuthMethod_emailInRequestBody() {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("email", "user@test.org");

        ResponseEntity<?> response = controller.checkAuthMethod(null, requestBody);
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals("SSO", body.get("authMethod"));
        assertEquals(true, body.get("requiresSSO"));
    }
}
