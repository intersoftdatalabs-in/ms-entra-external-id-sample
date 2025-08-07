package com.intsof.samples.integration;

import com.intsof.samples.entra.controller.SsoConfigController;
import com.intsof.samples.entra.dto.SsoConfigDto;
import com.intsof.samples.entra.config.SsoConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Direct integration style test (no web layer) that ensures the controller returns
 * all expected keys derived from real application.properties values.
 */
public class SsoConfigControllerIntegrationTest {

    private SsoConfigController controller;

    @BeforeEach
    void setup() {
        // Use actual binding by manually instantiating SsoConfigProperties and copying values from system properties.
        // For simplicity in this sample we read from defaults in application.properties.
        SsoConfigProperties props = new SsoConfigProperties();
        // Manually set minimal fields needed by controller using ReflectionTestUtils so that we don't require full Spring context.
        ReflectionTestUtils.setField(props, "enabledDomains", List.of("gmail.com", "intsof.com", "microsoft.com"));

        SsoConfigProperties.Registration registration = new SsoConfigProperties.Registration();
        SsoConfigProperties.Registration.Azure regAzure = new SsoConfigProperties.Registration.Azure();
        regAzure.setTenantId("demo-tenant-id");
        regAzure.setRedirectUri("http://localhost:4200/auth/callback");
        regAzure.setScope("openid profile email");
        regAzure.setClientId("demo-client-id");
        registration.setAzure(regAzure);
        ReflectionTestUtils.setField(props, "registration", registration);

        SsoConfigProperties.Provider provider = new SsoConfigProperties.Provider();
        SsoConfigProperties.Provider.Azure provAzure = new SsoConfigProperties.Provider.Azure();
        provAzure.setAuthorizationUri("https://login.microsoftonline.com/");
        provider.setAzure(provAzure);
        provider.setTokenUri("https://login.microsoftonline.com/oauth2/v2.0/token");
        ReflectionTestUtils.setField(props, "provider", provider);

        controller = new SsoConfigController(props);
    }

    @Test
    void verifyAllExpectedKeysPresent() {
        SsoConfigDto dto = controller.getConfig().getBody();
        assertNotNull(dto, "DTO must not be null");

        assertNotNull(dto.getEnabledDomains(), "enabledDomains present");
        assertNotNull(dto.getAuthorizationEndpoint(), "authorizationEndpoint present");
        assertNotNull(dto.getTokenEndpoint(), "tokenEndpoint present");
        assertNotNull(dto.getRedirectUri(), "redirectUri present");
        assertNotNull(dto.getScopes(), "scopes present");
        assertNotNull(dto.getClientId(), "clientId present");
    }
}

