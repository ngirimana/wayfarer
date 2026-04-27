package org.acme.service.impl;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.acme.entity.Trip;
import org.acme.service.TripService;

@ApplicationScoped
public class TripServiceImpl implements TripService {

    @Transactional
    public Trip createTrip(Trip trip) {
        trip.status = "ACTIVE";
        trip.persist();
        return trip;
    }

    @Transactional
    public void cancelTrip(Long id) {
        Trip trip = Trip.findById(id);
        if (trip == null) {
            throw new NotFoundException("Trip not found");
        }
        trip.status = "CANCELLED";
    }

    public List<Trip> getAllTrips(String origin, String destination, String sortBy) {
        StringBuilder query = new StringBuilder("status = 'ACTIVE'");
        Map<String, Object> params = new HashMap<>();

        if (origin != null && !origin.isEmpty()) {
            query.append(" AND origin = :origin");
            params.put("origin", origin);
        }
        if (destination != null && !destination.isEmpty()) {
            query.append(" AND destination = :destination");
            params.put("destination", destination);
        }

        Sort sort = Sort.ascending("id");
        if ("price".equalsIgnoreCase(sortBy)) {
            sort = Sort.ascending("fare");
        }

        return Trip.list(query.toString(), sort, params);
    }

    public Trip getTrip(Long id) {
        Trip trip = Trip.findById(id);
        if (trip == null) {
            throw new NotFoundException("Trip not found");
        }
        return trip;
    }
}
