package org.acme.service;

import java.util.List;
import org.acme.entity.Trip;

public interface TripService {
    Trip createTrip(Trip trip);
    void cancelTrip(Long id);
    List<Trip> getAllTrips(String origin, String destination, String sortBy);
    Trip getTrip(Long id);
}
