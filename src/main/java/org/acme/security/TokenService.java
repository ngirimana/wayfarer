package org.acme.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Set;
import org.acme.entity.User;

@ApplicationScoped
public class TokenService {

    public String generateToken(User user) {
        return Jwt.issuer("https://wayfare.com")
                .upn(user.email)
                .groups(Set.of(user.role))
                .claim("phone", user.phone)
                .expiresIn(3600) // 1 hour
                .sign();
    }
}
