package com.btech_major_project.Personal_Cloud.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Must be at least 256 bits (32 bytes) for HMAC-SHA256
        String secret = "this-is-a-very-long-secret-key-for-testing-purposes-123456";
        jwtService = new JwtService(secret, 3600000); // 1 hour expiration
    }

    @Test
    void testGenerateAndExtractToken() {
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);

        String subject = jwtService.extractSubject(token);
        assertEquals("testuser", subject);
    }

    @Test
    void testIsValid_Success() {
        String token = jwtService.generateToken("testuser");
        assertTrue(jwtService.isValid(token));
    }

    @Test
    void testIsValid_FailsForInvalidToken() {
        assertFalse(jwtService.isValid("invalid.token.here"));
    }

    @Test
    void testIsValid_FailsForExpiredToken() {
        JwtService shortLivedService = new JwtService("this-is-a-very-long-secret-key-for-testing-purposes-123456", -1000); // Expired 1 sec ago
        String token = shortLivedService.generateToken("testuser");
        assertFalse(jwtService.isValid(token));
    }
}
