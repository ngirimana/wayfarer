package org.acme.security;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class TokenServiceTest {

    @Inject
    TokenService tokenService;

    @Test
    public void testGenerateToken() {
        User user = new User();
        user.email = "test@domain.com";
        user.role = "USER";
        user.phone = "0780000000";

        String token = tokenService.generateToken(user);
        
        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "Token should have 3 parts (Header, Payload, Signature)");
    }
}
