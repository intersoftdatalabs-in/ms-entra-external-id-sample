package com.intsof.samples.entra.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("password");
    }

    @Test
    void testDefaultRoleAssignment() {
        user.assignDefaultRole();
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().contains("USER"));
    }

    @Test
    void testSetAndGetRoles() {
        user.setRoles(List.of("ADMIN", "USER"));
        assertEquals(List.of("ADMIN", "USER"), user.getRoles());
    }
}
