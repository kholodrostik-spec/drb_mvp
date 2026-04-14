package com.drb.DrbMVP.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "MySuperSecretKeyForTestingPurposesOnly123456";
    private static final long EXPIRATION = 3600000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken("test@example.com", "ROLE_USER");
        Assertions.assertNotNull(token);
        Assertions.assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String email = "test@example.com";
        String token = jwtService.generateToken(email, "ROLE_USER");

        Assertions.assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtService.generateToken("test@example.com", "ROLE_ADMIN");

        Assertions.assertEquals("ROLE_ADMIN", jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken("test@example.com", "ROLE_USER");

        Assertions.assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        Assertions.assertFalse(jwtService.isTokenValid("this.is.not.valid"));
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateToken("test@example.com", "ROLE_USER");

        Assertions.assertFalse(jwtService.isTokenValid(token));
    }
}
