package org.acme.mock;

import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.entity.Booking;
import org.acme.entity.User;
import org.acme.service.impl.BookingServiceImpl;

import java.util.ArrayList;
import java.util.List;

@Mock
@ApplicationScoped
public class MockBookingService extends BookingServiceImpl {

    @Override
    public List<Booking> getBookings(User user) {
        return new ArrayList<>();
    }

    @Override
    public Booking bookSeat(User user, Long tripId, int seatNumber) {
        if (tripId == 999L) {
            throw new jakarta.ws.rs.BadRequestException("Trip not available");
        }
        if (tripId == 888L) {
            throw new org.acme.exception.ConflictException("Seat already booked");
        }
        Booking booking = new Booking();
        booking.id = 1L;
        booking.seatNumber = seatNumber;
        return booking;
    }
    @Override
    public void deleteBooking(User user, Long bookingId) {
        if (bookingId == 777L) {
            throw new jakarta.ws.rs.BadRequestException("Unauthorized to delete this booking");
        }
    }
}
