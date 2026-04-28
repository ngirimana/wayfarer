package org.acme.service.impl;
 
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import org.acme.entity.Booking;
import org.acme.entity.Trip;
import org.acme.entity.User;
import org.acme.exception.ConflictException;
import org.acme.service.BookingService;
import org.jboss.logging.Logger;
 
@ApplicationScoped
public class BookingServiceImpl implements BookingService {
 
    private static final Logger LOG = Logger.getLogger(BookingServiceImpl.class);
 
    @Transactional
    public Booking bookSeat(User user, Long tripId, int seatNumber) {
        Trip trip = Trip.findById(tripId);
        LOG.debugf("Booking attempt - User: %s, Trip: %d, Seat: %d", user.email, tripId, seatNumber);
        
        if (trip == null || !"ACTIVE".equals(trip.status)) {
            throw new BadRequestException("Trip not available or inactive");
        }
 
        if (seatNumber <= 0 || seatNumber > trip.capacity) {
            throw new BadRequestException("Invalid seat number. Valid range: 1 to " + trip.capacity);
        }
 
        // Check if seat already booked
        long existing = Booking.count("trip = ?1 AND seatNumber = ?2", trip, seatNumber);
        if (existing > 0) {
            throw new ConflictException("Seat " + seatNumber + " is already booked for this trip");
        }
 
        Booking booking = new Booking();
        booking.user = user;
        booking.trip = trip;
        booking.seatNumber = seatNumber;
        booking.farePaid = calculateFareWithDiscount(user, trip.fare);
        
        booking.persistAndFlush();
        LOG.infof("Seat booked successfully: Booking ID %d for user %s", booking.id, user.email);
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
            LOG.warnf("Unauthorized deletion attempt by user %s on booking %d", user.email, bookingId);
            throw new ForbiddenException("Unauthorized to delete this booking");
        }
 
        booking.delete();
        LOG.infof("Booking %d deleted by user %s", bookingId, user.email);
    }
 
    public double calculateFareWithDiscount(User user, double baseFare) {
        long userBookings = Booking.count("user", user);
        if (userBookings >= 3) {
            return baseFare * 0.95; // 5% discount for loyal customers (3+ bookings)
        }
        return baseFare;
    }
}
