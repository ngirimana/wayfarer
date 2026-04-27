package org.acme.config;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.User;
import org.acme.security.PasswordUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DataInitializer {

    @Inject
    PasswordUtils passwordUtils;

    @ConfigProperty(name = "wayfare.admin.email")
    String adminEmail;

    @ConfigProperty(name = "wayfare.admin.password")
    String adminPassword;

    @Transactional
    public void onStart(@Observes StartupEvent ev) {
        if (User.count() == 0) {
            User admin = new User();
            admin.email = adminEmail;
            admin.phone = "0000000000";
            admin.password = passwordUtils.hashPassword(adminPassword);
            admin.role = "ADMIN";
            admin.isVerified = true;
            admin.persist();
        }
    }
}
