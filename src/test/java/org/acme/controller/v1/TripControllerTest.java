package org.acme.controller.v1;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.acme.entity.Trip;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
public class TripControllerTest {

    // ==================== GET ALL TRIPS TESTS ====================

    @Test
    public void testGetAllTripsPublicAccess() {
        given()
          .when().get("/api/v1/trips")
          .then()
             .statusCode(200)
             .contentType(ContentType.JSON)
             .body("size()", greaterThanOrEqualTo(0));
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testGetAllTripsAsUser() {
        given()
          .when().get("/api/v1/trips")
          .then()
             .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    public void testGetAllTripsAsAdmin() {
        given()
          .when().get("/api/v1/trips")
          .then()
             .statusCode(200);
    }

    @Test
    public void testSearchTripsByOrigin() {
        given()
          .queryParam("origin", "Kigali")
          .when().get("/api/v1/trips")
          .then()
             .statusCode(200);
    }

    @Test
    public void testSearchTripsByDestination() {
        given()
          .queryParam("destination", "Musanze")
          .when().get("/api/v1/trips")
          .then()
             .statusCode(200);
    }

    @Test
    public void testSearchTripsByRoute() {
        given()
          .queryParam("origin", "Kigali")
          .queryParam("destination", "Huye")
          .when().get("/api/v1/trips")
          .then()
             .statusCode(200);
    }

    // ==================== GET SINGLE TRIP TESTS ====================

    @Test
    public void testGetTripByIdFound() {
        PanacheMock.mock(Trip.class);
        Trip mockTrip = new Trip();
        mockTrip.id = 1L;
        mockTrip.origin = "Kigali";
        mockTrip.destination = "Musanze";
        Mockito.when(Trip.findById(1L)).thenReturn(mockTrip);

        given()
          .when().get("/api/v1/trips/1")
          .then()
             .statusCode(200)
             .body("origin", is("Kigali"))
             .body("destination", is("Musanze"));
    }

    @Test
    public void testGetTripByIdNotFound() {
        PanacheMock.mock(Trip.class);
        Mockito.when(Trip.findById(Mockito.anyLong())).thenReturn(null);

        given()
          .when().get("/api/v1/trips/9999")
          .then()
             .statusCode(404);
    }

    // ==================== CREATE TRIP TESTS ====================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    public void testCreateTripAsAdminSuccess() {
        Trip trip = new Trip();
        trip.origin = "Kigali";
        trip.destination = "Musanze";
        trip.fare = 5000.0;
        trip.capacity = 30;
        trip.tripDate = LocalDateTime.now().plusDays(1);
        trip.status = "ACTIVE";

        given()
          .contentType(ContentType.JSON)
          .body(trip)
          .when().post("/api/v1/trips")
          .then()
             .statusCode(201);
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    public void testCreateTripWithDifferentRoute() {
        Trip trip = new Trip();
        trip.origin = "Butare";
        trip.destination = "Ruhengeri";
        trip.fare = 3000.0;
        trip.capacity = 20;
        trip.tripDate = LocalDateTime.now().plusDays(2);
        trip.status = "ACTIVE";

        given()
          .contentType(ContentType.JSON)
          .body(trip)
          .when().post("/api/v1/trips")
          .then()
             .statusCode(201);
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testCreateTripAsUserForbidden() {
        Trip trip = new Trip();
        trip.origin = "Kigali";
        trip.destination = "Huye";
        trip.fare = 2500.0;

        given()
          .contentType(ContentType.JSON)
          .body(trip)
          .when().post("/api/v1/trips")
          .then()
             .statusCode(403);
    }

    @Test
    public void testCreateTripAnonymousDenied() {
        given()
          .contentType(ContentType.JSON)
          .body("{\"origin\":\"Kigali\",\"destination\":\"Musanze\"}")
          .when().post("/api/v1/trips")
          .then()
             .statusCode(401);
    }

    // ==================== CANCEL TRIP TESTS ====================

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    public void testCancelTripSuccess() {
        PanacheMock.mock(Trip.class);
        Trip mockTrip = new Trip();
        mockTrip.id = 1L;
        mockTrip.status = "ACTIVE";
        Mockito.when(Trip.findById(1L)).thenReturn(mockTrip);

        given()
          .when().patch("/api/v1/trips/1/cancel")
          .then()
             .statusCode(200)
             .body("message", is("Trip cancelled successfully."));
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    public void testCancelTripNotFound() {
        PanacheMock.mock(Trip.class);
        Mockito.when(Trip.findById(Mockito.anyLong())).thenReturn(null);

        given()
          .when().patch("/api/v1/trips/9999/cancel")
          .then()
             .statusCode(404);
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testCancelTripAsUserForbidden() {
        given()
          .when().patch("/api/v1/trips/1/cancel")
          .then()
             .statusCode(403);
    }

    @Test
    public void testCancelTripAnonymousDenied() {
        given()
          .when().patch("/api/v1/trips/1/cancel")
          .then()
             .statusCode(401);
    }
}
