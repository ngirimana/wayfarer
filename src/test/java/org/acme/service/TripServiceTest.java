package org.acme.service;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.acme.entity.Trip;
import org.acme.service.impl.TripServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

@QuarkusTest
public class TripServiceTest {

    @Inject
    TripServiceImpl tripService;

    @Test
    public void testGetTripNotFound() {
        PanacheMock.mock(Trip.class);
        Mockito.when(Trip.findById(Mockito.anyLong())).thenReturn(null);

        Assertions.assertThrows(NotFoundException.class, () -> {
            tripService.getTrip(1L);
        });
    }

    @Test
    public void testGetTripSuccess() {
        Trip mockTrip = new Trip();
        mockTrip.id = 1L;
        mockTrip.origin = "Kigali";
        mockTrip.destination = "Musanze";

        PanacheMock.mock(Trip.class);
        Mockito.when(Trip.findById(1L)).thenReturn(mockTrip);

        Trip trip = tripService.getTrip(1L);
        Assertions.assertNotNull(trip);
        Assertions.assertEquals("Kigali", trip.origin);
    }

    @Test
    public void testCreateTrip() {
        Trip trip = new Trip();
        trip.origin = "Kigali";
        trip.destination = "Rubavu";
        trip.tripDate = LocalDateTime.now().plusDays(1);
        trip.fare = 5000.0;
        trip.capacity = 30;

        PanacheMock.mock(Trip.class);
        
        Trip createdTrip = tripService.createTrip(trip);
        
        Assertions.assertEquals("ACTIVE", createdTrip.status);
    }

    @Test
    public void testCancelTripSuccess() {
        Trip mockTrip = new Trip();
        mockTrip.id = 1L;
        mockTrip.status = "ACTIVE";

        PanacheMock.mock(Trip.class);
        Mockito.when(Trip.findById(1L)).thenReturn(mockTrip);

        tripService.cancelTrip(1L);
        Assertions.assertEquals("CANCELLED", mockTrip.status);
    }

    @Test
    public void testCancelTripNotFound() {
        PanacheMock.mock(Trip.class);
        Mockito.when(Trip.findById(Mockito.anyLong())).thenReturn(null);

        Assertions.assertThrows(NotFoundException.class, () -> {
            tripService.cancelTrip(999L);
        });
    }

    @Test
    public void testGetAllTrips() {
        PanacheMock.mock(Trip.class);
        Mockito.when(Trip.list(Mockito.anyString(), Mockito.any(), Mockito.anyMap())).thenReturn(java.util.Collections.emptyList());

        // Test with no filters
        tripService.getAllTrips(null, null, null);
        
        // Test with filters and price sort
        tripService.getAllTrips("Kigali", "Musanze", "price");

        // Test with empty string filters and different sort
        tripService.getAllTrips("", "", "date");
    }
}
