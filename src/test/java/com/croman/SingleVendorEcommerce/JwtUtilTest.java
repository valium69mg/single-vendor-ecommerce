package com.croman.SingleVendorEcommerce;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.SingleVendorEcommerce.Jwt.JwtUtil;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil = new JwtUtil("mysecretkeymysecretkeymysecretkey", 3600000); 

    @Test
    void testGenerateAndExtractUsername() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void testValidateToken_validToken() {
        String token = jwtUtil.generateToken("validuser");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_invalidToken() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        JwtUtil shortLivedJwtUtil = new JwtUtil("anothersecretkeyanothersecretkey", 1); // 1 ms
        String token = shortLivedJwtUtil.generateToken("expiringuser");

        Thread.sleep(10); 
        assertFalse(shortLivedJwtUtil.validateToken(token));
    }
}
