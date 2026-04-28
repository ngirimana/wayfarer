package org.acme.config;

import io.quarkus.runtime.StartupEvent;
import org.acme.entity.User;
import org.acme.security.PasswordUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import java.util.UUID;

@QuarkusTest
public class DataInitializerTest {

    @Test
    public void testDataInitializer() {
        DataInitializer initializer = new DataInitializer();
        initializer.adminEmail = "admin@example.com";
        initializer.adminPassword = "adminPassword";
        
        initializer.passwordUtils = Mockito.mock(PasswordUtils.class);
        Mockito.when(initializer.passwordUtils.hashPassword(Mockito.anyString())).thenReturn("hashedPassword");

        StartupEvent event = new StartupEvent();

        QuarkusTransaction.requiringNew().run(() -> {
            org.acme.entity.Booking.deleteAll();
            User.deleteAll();
            initializer.onStart(event);
            Assertions.assertEquals(1, User.count());
            
            // Cover User.findByEmail
            User admin = User.findByEmail("admin@example.com");
            Assertions.assertNotNull(admin);
            Assertions.assertNull(User.findByEmail("nonexistent@example.com"));
            
            // Call again to test the count() > 0 branch
            initializer.onStart(event);
            Assertions.assertEquals(1, User.count());
        });
    }
}
