package org.acme.controller.v1;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.entity.User;
import org.acme.payload.request.BookingRequest;
import org.acme.service.BookingService;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingController {

    @Inject
    BookingService bookingService;

    @Inject
    JsonWebToken jwt;

    @POST
    @RolesAllowed("USER")
    public Response bookSeat(@Valid BookingRequest request) {
        User user = User.findByEmail(jwt.getName());
        return Response.status(Response.Status.CREATED)
                .entity(bookingService.bookSeat(user, request.tripId, request.seatNumber))
                .build();
    }

    @GET
    @RolesAllowed({"ADMIN", "USER"})
    public Response getBookings() {
        User user = User.findByEmail(jwt.getName());
        return Response.ok(bookingService.getBookings(user)).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed({"ADMIN", "USER"})
    public Response deleteBooking(@PathParam("id") Long id) {
        User user = User.findByEmail(jwt.getName());
        bookingService.deleteBooking(user, id);
        return Response.noContent().build();
    }
}
