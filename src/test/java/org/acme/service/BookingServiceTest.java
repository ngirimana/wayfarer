package org.acme.service;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import org.acme.entity.Booking;
import org.acme.entity.Trip;
import org.acme.entity.User;
import org.acme.exception.ConflictException;
import org.acme.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class BookingServiceTest {

    BookingServiceImpl bookingService;

    @BeforeEach
    public void setup() {
        PanacheMock.mock(Trip.class);
        PanacheMock.mock(Booking.class);
        bookingService = new BookingServiceImpl();
    }

    @Test
    public void testBookSeatSuccess() {
        User user = new User();
        user.email = java.util.UUID.randomUUID().toString() + "@example.com";
        user.phone = java.util.UUID.randomUUID().toString().substring(0, 15);
        user.password = "pass";
        user.role = "USER";
        QuarkusTransaction.requiringNew().run(() -> user.persist());

        Trip trip = new Trip();
        trip.origin = "A";
        trip.destination = "B";
        trip.tripDate = java.time.LocalDateTime.now().plusDays(1);
        trip.fare = 100.0;
        trip.capacity = 10;
        trip.status = "ACTIVE";
        QuarkusTransaction.requiringNew().run(() -> trip.persist());

        Mockito.when(Trip.findById(Mockito.any())).thenReturn(trip);
        Mockito.when(Booking.count(Mockito.anyString(), (Object[]) Mockito.any())).thenReturn(0L);

        Booking booking = QuarkusTransaction.requiringNew().call(() -> bookingService.bookSeat(user, trip.id, 5));
        assertNotNull(booking);
        assertEquals(100.0, booking.farePaid);
    }

    @Test
    public void testBookSeatTripNotAvailable() {
        User user = new User();
        user.id = 1L;

        Mockito.when(Trip.findById(Mockito.any())).thenReturn(null);
        assertThrows(BadRequestException.class, () -> QuarkusTransaction.requiringNew().call(() -> bookingService.bookSeat(user, 2L, 5)));

        Trip inactiveTrip = new Trip();
        inactiveTrip.id = 3L;
        inactiveTrip.status = "CANCELLED";
        Mockito.when(Trip.findById(Mockito.any())).thenReturn(inactiveTrip);
        assertThrows(BadRequestException.class, () -> QuarkusTransaction.requiringNew().call(() -> bookingService.bookSeat(user, 3L, 5)));
    }

    @Test
    public void testBookSeatInvalidSeatNumber() {
        User user = new User();
        user.id = 1L;
        Trip trip = new Trip();
        trip.id = 1L;
        trip.status = "ACTIVE";
        trip.capacity = 10;

        Mockito.when(Trip.findById(Mockito.any())).thenReturn(trip);

        assertThrows(BadRequestException.class, () -> QuarkusTransaction.requiringNew().call(() -> bookingService.bookSeat(user, 1L, 15)));
        assertThrows(BadRequestException.class, () -> QuarkusTransaction.requiringNew().call(() -> bookingService.bookSeat(user, 1L, 0)));
    }

    @Test
    public void testBookSeatAlreadyBooked() {
        User user = new User();
        user.id = 1L;
        Trip trip = new Trip();
        trip.id = 1L;
        trip.status = "ACTIVE";
        trip.capacity = 20;

        Mockito.when(Trip.findById(Mockito.any())).thenReturn(trip);
        Mockito.when(Booking.count(Mockito.anyString(), (Object[]) Mockito.any())).thenReturn(1L);

        assertThrows(ConflictException.class, () -> QuarkusTransaction.requiringNew().call(() -> bookingService.bookSeat(user, 1L, 12)));
    }

    @Test
    public void testGetBookings() {
        User admin = new User();
        admin.role = "ADMIN";
        
        User user = new User();
        user.role = "USER";

        Mockito.when(Booking.listAll()).thenReturn(Collections.singletonList(new Booking()));
        Mockito.when(Booking.list(Mockito.anyString(), Mockito.any(Object.class))).thenReturn(Collections.emptyList());

        List<Booking> adminBookings = bookingService.getBookings(admin);
        assertEquals(1, adminBookings.size());

        List<Booking> userBookings = bookingService.getBookings(user);
        assertEquals(0, userBookings.size());
    }

    @Test
    public void testDeleteBookingSuccess() {
        User user = new User();
        user.email = java.util.UUID.randomUUID().toString() + "@example.com";
        user.phone = java.util.UUID.randomUUID().toString().substring(0, 15);
        user.password = "pass";
        user.role = "USER";
        QuarkusTransaction.requiringNew().run(() -> user.persist());

        Trip trip = new Trip();
        trip.origin = "A";
        trip.destination = "B";
        trip.tripDate = java.time.LocalDateTime.now().plusDays(1);
        trip.fare = 100.0;
        trip.capacity = 10;
        trip.status = "ACTIVE";
        QuarkusTransaction.requiringNew().run(() -> trip.persist());

        Booking booking = new Booking();
        booking.user = user;
        booking.trip = trip;
        booking.seatNumber = 1;
        booking.farePaid = 100.0;
        QuarkusTransaction.requiringNew().run(() -> booking.persist());

        Mockito.when(Booking.findById(Mockito.any())).thenReturn(booking);
        assertDoesNotThrow(() -> QuarkusTransaction.requiringNew().run(() -> bookingService.deleteBooking(user, booking.id)));
    }

    @Test
    public void testDeleteBookingAdmin() {
        User admin = new User();
        admin.email = java.util.UUID.randomUUID().toString() + "@example.com";
        admin.phone = java.util.UUID.randomUUID().toString().substring(0, 15);
        admin.password = "pass";
        admin.role = "ADMIN";
        QuarkusTransaction.requiringNew().run(() -> admin.persist());

        User user = new User();
        user.email = java.util.UUID.randomUUID().toString() + "@example.com";
        user.phone = java.util.UUID.randomUUID().toString().substring(0, 15);
        user.password = "pass";
        user.role = "USER";
        QuarkusTransaction.requiringNew().run(() -> user.persist());

        Trip trip = new Trip();
        trip.origin = "A";
        trip.destination = "B";
        trip.tripDate = java.time.LocalDateTime.now().plusDays(1);
        trip.fare = 100.0;
        trip.capacity = 10;
        trip.status = "ACTIVE";
        QuarkusTransaction.requiringNew().run(() -> trip.persist());

        Booking booking = new Booking();
        booking.user = user;
        booking.trip = trip;
        booking.seatNumber = 1;
        booking.farePaid = 100.0;
        QuarkusTransaction.requiringNew().run(() -> booking.persist());

        Mockito.when(Booking.findById(Mockito.any())).thenReturn(booking);
        assertDoesNotThrow(() -> QuarkusTransaction.requiringNew().run(() -> bookingService.deleteBooking(admin, booking.id)));
    }

    @Test
    public void testDeleteBookingNotFound() {
        User user = new User();
        user.id = 1L;
        Mockito.when(Booking.findById(Mockito.any())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> QuarkusTransaction.requiringNew().run(() -> bookingService.deleteBooking(user, 999L)));
    }

    @Test
    public void testDeleteBookingUnauthorized() {
        User user1 = new User();
        user1.id = 1L;
        user1.role = "USER";

        User user2 = new User();
        user2.id = 2L;
        user2.role = "USER";

        Booking booking = new Booking();
        booking.id = 102L;
        booking.user = user2;

        Mockito.when(Booking.findById(Mockito.any())).thenReturn(booking);
        assertThrows(ForbiddenException.class, () -> QuarkusTransaction.requiringNew().run(() -> bookingService.deleteBooking(user1, 102L)));
    }

    @Test
    public void testCalculateFareWithDiscount() {
        User user = new User();
        user.id = 1L;
        
        Mockito.when(Booking.count(Mockito.anyString(), Mockito.any(Object.class))).thenReturn(2L);
        double fareNoDiscount = bookingService.calculateFareWithDiscount(user, 100.0);
        assertEquals(100.0, fareNoDiscount);

        Mockito.when(Booking.count(Mockito.anyString(), Mockito.any(Object.class))).thenReturn(3L);
        double fareDiscount = bookingService.calculateFareWithDiscount(user, 100.0);
        assertEquals(95.0, fareDiscount);
    }
}
