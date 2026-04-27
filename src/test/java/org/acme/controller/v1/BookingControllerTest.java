package org.acme.controller.v1;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.acme.entity.Booking;
import org.acme.entity.User;
import org.acme.payload.request.BookingRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class BookingControllerTest {

    // ==================== GET BOOKINGS TESTS ====================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testGetBookingsAsUser() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        given()
          .when().get("/api/v1/bookings")
          .then()
             .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin@example.com", roles = {"ADMIN"})
    public void testGetBookingsAsAdmin() {
        PanacheMock.mock(User.class);
        User admin = new User();
        admin.role = "ADMIN";
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(admin);

        given()
          .when().get("/api/v1/bookings")
          .then()
             .statusCode(200);
    }

    @Test
    public void testGetBookingsUnauthenticated() {
        given()
          .when().get("/api/v1/bookings")
          .then()
             .statusCode(401);
    }

    // ==================== BOOK SEAT TESTS ====================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testBookSeatSuccess() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        BookingRequest request = new BookingRequest();
        request.tripId = 1L;
        request.seatNumber = 5;

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/bookings")
          .then()
             .statusCode(201);
    }

    @Test
    public void testBookSeatUnauthenticated() {
        BookingRequest request = new BookingRequest();
        request.tripId = 1L;
        request.seatNumber = 5;

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/bookings")
          .then()
             .statusCode(401);
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testBookSeatTripNotAvailable() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        BookingRequest request = new BookingRequest();
        request.tripId = 999L; // Triggers BadRequestException in MockBookingService
        request.seatNumber = 5;

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/bookings")
          .then()
             .statusCode(400);
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testBookSeatAlreadyTaken() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        BookingRequest request = new BookingRequest();
        request.tripId = 888L; // Triggers ConflictException in MockBookingService
        request.seatNumber = 5;

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/bookings")
          .then()
             .statusCode(409);
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testBookSeatMissingTripId() {
        BookingRequest request = new BookingRequest();
        request.seatNumber = 5;
        // tripId is null

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/bookings")
          .then()
             .statusCode(400);
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testBookSeatDifferentSeats() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        BookingRequest request = new BookingRequest();
        request.tripId = 1L;
        request.seatNumber = 10;

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/bookings")
          .then()
             .statusCode(201);
    }

    // ==================== DELETE BOOKING TESTS ====================

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testDeleteBookingSuccess() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        given()
          .when().delete("/api/v1/bookings/1")
          .then()
             .statusCode(204);
    }

    @Test
    @TestSecurity(user = "user@example.com", roles = {"USER"})
    public void testDeleteBookingUnauthorized() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        given()
          .when().delete("/api/v1/bookings/777")
          .then()
             .statusCode(400);
    }

    @Test
    public void testDeleteBookingUnauthenticated() {
        given()
          .when().delete("/api/v1/bookings/1")
          .then()
             .statusCode(401);
    }
}
