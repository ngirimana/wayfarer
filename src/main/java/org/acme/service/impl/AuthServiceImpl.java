package org.acme.service.impl;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.Random;
import org.acme.entity.User;
import org.acme.exception.ConflictException;
import org.acme.payload.request.LoginRequest;
import org.acme.payload.request.SignupRequest;
import org.acme.payload.request.VerifyRequest;
import org.acme.payload.response.AuthResponse;
import org.acme.security.PasswordUtils;
import org.acme.security.TokenService;
import org.acme.service.AuthService;
import org.jboss.logging.Logger;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);

    @Inject
    PasswordUtils passwordUtils;

    @Inject
    TokenService tokenService;

    @Inject
    Mailer mailer;

    @Transactional
    public void signup(SignupRequest request) {
        if (User.findByEmail(request.email) != null) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.email = request.email;
        user.phone = request.phone;
        user.password = passwordUtils.hashPassword(request.password);
        user.role = "USER"; // Always default to USER for public signup
        user.otp = generateOTP();
        user.otpExpiry = LocalDateTime.now().plusMinutes(10);
        user.persist();

        LOG.info("OTP for " + user.email + " is: " + user.otp);
        mailer.send(Mail.withText(user.email, "Wayfare Verification", "Your OTP is: " + user.otp));
    }

    @Transactional
    public void verify(VerifyRequest request) {
        User user = User.findByEmail(request.email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (user.isVerified) {
            throw new BadRequestException(Response.status(400).entity(new org.acme.payload.response.MessageResponse("User already verified")).build());
        }

        if (user.otp == null || !user.otp.equals(request.otp)) {
            throw new BadRequestException(Response.status(400).entity(new org.acme.payload.response.MessageResponse("Invalid OTP")).build());
        }

        if (user.otpExpiry.isBefore(LocalDateTime.now())) {
            throw new BadRequestException(Response.status(400).entity(new org.acme.payload.response.MessageResponse("OTP expired")).build());
        }

        user.isVerified = true;
        user.otp = null;
        user.otpExpiry = null;
    }

    public AuthResponse login(LoginRequest request) {
        User user = User.findByEmail(request.email);
        if (user == null || !passwordUtils.checkPassword(request.password, user.password)) {
            throw new BadRequestException(Response.status(400).entity(new org.acme.payload.response.MessageResponse("Invalid credentials")).build());
        }

        if (!user.isVerified) {
            throw new BadRequestException(Response.status(400).entity(new org.acme.payload.response.MessageResponse("Account not verified")).build());
        }

        String token = tokenService.generateToken(user);
        return new AuthResponse(token, user.email, user.role);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        User user = User.findByEmail(email);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        user.otp = generateOTP();
        user.otpExpiry = LocalDateTime.now().plusMinutes(10);
        LOG.info("Password Reset OTP for " + user.email + " is: " + user.otp);
        mailer.send(Mail.withText(user.email, "Wayfare Password Reset", "Your reset OTP is: " + user.otp));
    }

    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = User.findByEmail(email);
        if (user == null || user.otp == null || !user.otp.equals(otp)) {
            throw new BadRequestException(Response.status(400).entity(new org.acme.payload.response.MessageResponse("Invalid OTP or User")).build());
        }

        if (user.otpExpiry.isBefore(LocalDateTime.now())) {
            throw new BadRequestException(Response.status(400).entity(new org.acme.payload.response.MessageResponse("OTP expired")).build());
        }

        user.password = passwordUtils.hashPassword(newPassword);
        user.otp = null;
        user.otpExpiry = null;
    }

    private String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
