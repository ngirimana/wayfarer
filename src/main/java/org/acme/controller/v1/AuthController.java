package org.acme.controller.v1;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.payload.request.LoginRequest;
import org.acme.payload.request.SignupRequest;
import org.acme.payload.request.VerifyRequest;
import org.acme.payload.response.MessageResponse;
import org.acme.service.AuthService;

@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {

    @Inject
    AuthService authService;

    @POST
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response signup(@Valid SignupRequest request) {
        authService.signup(request);
        return Response.status(Response.Status.CREATED)
                .entity(new MessageResponse("User registered. Please verify with OTP sent to your email/phone."))
                .build();
    }

    @POST
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verify(@Valid VerifyRequest request) {
        authService.verify(request);
        return Response.ok(new MessageResponse("Account verified successfully")).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginRequest request) {
        return Response.ok(authService.login(request)).build();
    }

    @POST
    @Path("/reset-password-request")
    public Response requestReset(@QueryParam("email") String email) {
        authService.requestPasswordReset(email);
        return Response.ok(new MessageResponse("OTP sent to your email/phone.")).build();
    }

    @POST
    @Path("/reset-password")
    public Response reset(@QueryParam("email") String email, @QueryParam("otp") String otp, @QueryParam("password") String password) {
        authService.resetPassword(email, otp, password);
        return Response.ok(new MessageResponse("Password reset successfully.")).build();
    }
}
