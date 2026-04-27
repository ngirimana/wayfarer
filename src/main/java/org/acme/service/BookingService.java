package org.acme.service;

import java.util.List;
import org.acme.entity.Booking;
import org.acme.entity.User;

public interface BookingService {
    Booking bookSeat(User user, Long tripId, int seatNumber);
    List<Booking> getBookings(User user);
    void deleteBooking(User user, Long bookingId);
    double calculateFareWithDiscount(User user, double baseFare);
}
