package org.acme.controller.v1;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.acme.entity.Trip;
import org.acme.payload.response.MessageResponse;
import org.acme.service.TripService;

@Path("/api/v1/trips")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TripController {

    @Inject
    TripService tripService;

    @POST
    @RolesAllowed("ADMIN")
    public Response createTrip(@Valid Trip trip) {
        return Response.status(Response.Status.CREATED)
                .entity(tripService.createTrip(trip))
                .build();
    }

    @PATCH
    @Path("/{id}/cancel")
    @RolesAllowed("ADMIN")
    public Response cancelTrip(@PathParam("id") Long id) {
        tripService.cancelTrip(id);
        return Response.ok(new MessageResponse("Trip cancelled successfully.")).build();
    }

    @GET
    public List<Trip> getAllTrips(
            @QueryParam("origin") String origin,
            @QueryParam("destination") String destination,
            @QueryParam("sort") String sort) {
        return tripService.getAllTrips(origin, destination, sort);
    }

    @GET
    @Path("/{id}")
    public Trip getTrip(@PathParam("id") Long id) {
        return tripService.getTrip(id);
    }
}
