package org.acme.security;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordUtils {

    public String hashPassword(String password) {
        return BcryptUtil.bcryptHash(password);
    }

    public boolean checkPassword(String password, String hashed) {
        return BcryptUtil.matches(password, hashed);
    }
}
