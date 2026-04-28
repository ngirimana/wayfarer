package org.acme.security;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class PasswordUtilsTest {

    @Inject
    PasswordUtils passwordUtils;

    @Test
    public void testHashAndCheckPassword() {
        String plain = "mySecretPassword123";
        String hashed = passwordUtils.hashPassword(plain);

        assertNotNull(hashed);
        assertTrue(passwordUtils.checkPassword(plain, hashed));
        assertFalse(passwordUtils.checkPassword("wrongPassword", hashed));
    }
}
