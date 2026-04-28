package org.acme.entity;

import org.acme.payload.request.BookingRequest;
import org.acme.payload.request.LoginRequest;
import org.acme.payload.request.SignupRequest;
import org.acme.payload.request.VerifyRequest;
import org.acme.payload.response.AuthResponse;
import org.acme.payload.response.MessageResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class EntityCoverageTest {

    @Test
    public void testEntityConstructorsAndSetters() {
        // User
        User user = new User();
        user.email = "test@example.com";
        user.phone = "1234567890";
        user.password = "pass";
        user.role = "USER";
        user.otp = "123456";
        user.isVerified = true;
        user.otpExpiry = LocalDateTime.now();
        user.id = 1L;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();

        Assertions.assertEquals("test@example.com", user.email);

        // Trip
        Trip trip = new Trip();
        trip.id = 1L;
        trip.origin = "A";
        trip.destination = "B";
        trip.fare = 100.0;
        trip.capacity = 50;
        trip.status = "SCHEDULED";
        trip.tripDate = LocalDateTime.now();
        trip.createdAt = LocalDateTime.now();
        trip.updatedAt = LocalDateTime.now();

        Assertions.assertEquals("A", trip.origin);

        // Booking
        Booking booking = new Booking();
        booking.id = 1L;
        booking.seatNumber = 5;
        booking.user = user;
        booking.trip = trip;
        booking.createdAt = LocalDateTime.now();
        booking.updatedAt = LocalDateTime.now();

        Assertions.assertEquals(5, booking.seatNumber);
    }

    @Test
    public void testPayloadConstructors() {
        AuthResponse authResponse2 = new AuthResponse("token2", "email2", "role2");
        Assertions.assertEquals("token2", authResponse2.token);

        MessageResponse msgResponse = new MessageResponse();
        msgResponse.message = "msg";
        Assertions.assertEquals("msg", msgResponse.message);

        MessageResponse msgResponse2 = new MessageResponse("msg2");
        Assertions.assertEquals("msg2", msgResponse2.message);

        BookingRequest bookingReq = new BookingRequest();
        bookingReq.tripId = 1L;
        bookingReq.seatNumber = 2;
        Assertions.assertEquals(1L, bookingReq.tripId);

        LoginRequest loginReq = new LoginRequest();
        loginReq.email = "e";
        loginReq.password = "p";
        Assertions.assertEquals("e", loginReq.email);

        SignupRequest signupReq = new SignupRequest();
        signupReq.email = "e";
        signupReq.phone = "p";
        signupReq.password = "p";
        Assertions.assertEquals("e", signupReq.email);

        VerifyRequest verifyReq = new VerifyRequest();
        verifyReq.email = "e";
        verifyReq.otp = "o";
        Assertions.assertEquals("e", verifyReq.email);
        Assertions.assertEquals("o", verifyReq.otp);
        
        // Ensure AuthResponse getters are called
        authResponse2.getEmail();
        authResponse2.getRole();
        authResponse2.getToken();
    }
}
