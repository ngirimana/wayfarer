package org.acme.service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import org.acme.entity.Booking;
import org.acme.entity.Trip;
import org.acme.entity.User;
import org.acme.exception.ConflictException;
import org.acme.service.BookingService;

@ApplicationScoped
public class BookingServiceImpl implements BookingService {

    @Transactional
    public Booking bookSeat(User user, Long tripId, int seatNumber) {
        Trip trip = Trip.findById(tripId);
        if (trip == null || !"ACTIVE".equals(trip.status)) {
            throw new BadRequestException("Trip not available");
        }

        // Check if seat already booked
        long existing = Booking.count("trip = ?1 AND seatNumber = ?2", trip, seatNumber);
        if (existing > 0) {
            throw new ConflictException("Seat already booked");
        }

        Booking booking = new Booking();
        booking.user = user;
        booking.trip = trip;
        booking.seatNumber = seatNumber;
        
        // Discount logic: > 3 tickets -> 5% discount
        long userBookings = Booking.count("user", user);
        if (userBookings >= 3) {
            // In a real app, we'd store the paid amount in the Booking entity
            // For now, we'll just demonstrate the logic
        }

        booking.persist();
        return booking;
    }

    public List<Booking> getBookings(User user) {
        if ("ADMIN".equals(user.role)) {
            return Booking.listAll();
        } else {
            return Booking.list("user", user);
        }
    }

    @Transactional
    public void deleteBooking(User user, Long bookingId) {
        Booking booking = Booking.findById(bookingId);
        if (booking == null) {
            throw new NotFoundException("Booking not found");
        }

        if (!"ADMIN".equals(user.role) && !booking.user.id.equals(user.id)) {
            throw new BadRequestException("Unauthorized to delete this booking");
        }

        booking.delete();
    }

    public double calculateFareWithDiscount(User user, double baseFare) {
        long userBookings = Booking.count("user", user);
        if (userBookings >= 3) {
            return baseFare * 0.95;
        }
        return baseFare;
    }

    
}
