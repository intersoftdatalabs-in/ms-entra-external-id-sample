package com.intsof.samples.entra.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that SSO configuration values are correctly bound from application.properties
 * into the {@link SsoConfigProperties} bean.
 */
@SpringBootTest(classes = com.intsof.samples.entra.BackendApplication.class)
public class SsoConfigPropertiesTest {

    @Autowired
    private SsoConfigProperties props;

    @Test
    void testBindingEnabledDomains() {
        assertNotNull(props.getEnabledDomains());
        List<String> expected = List.of("gmail.com", "intsof.com", "microsoft.com");
        assertEquals(expected, props.getEnabledDomains());
    }

    @Test
    void testBindingRedirectUri() {
        assertNotNull(props.getRegistration().getAzure().getRedirectUri());
        assertTrue(props.getRegistration().getAzure().getRedirectUri().contains("https://"));
    }
}

