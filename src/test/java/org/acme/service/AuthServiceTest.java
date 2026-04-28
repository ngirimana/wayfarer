package org.acme.service;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.acme.entity.User;
import org.acme.payload.request.VerifyRequest;
import org.acme.payload.request.SignupRequest;
import org.acme.exception.ConflictException;
import org.acme.security.PasswordUtils;
import org.acme.security.TokenService;
import org.acme.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

@QuarkusTest
public class AuthServiceTest {

    @Inject
    AuthServiceImpl authService;

    @InjectMock
    PasswordUtils passwordUtils;

    @InjectMock
    TokenService tokenService;

    @BeforeEach
    public void setup() {
        PanacheMock.mock(User.class);
    }

    @Test
    public void testSignupSuccess() {
        Mockito.when(User.findByEmail(anyString())).thenReturn(null);
        SignupRequest req = new SignupRequest();
        req.email = "test@test.com";
        req.phone = "123456";
        req.password = "pass";
        Mockito.when(passwordUtils.hashPassword("pass")).thenReturn("hashed");

        assertDoesNotThrow(() -> authService.signup(req));
    }

    @Test
    public void testSignupConflict() {
        Mockito.when(User.findByEmail(anyString())).thenReturn(new User());
        SignupRequest req = new SignupRequest();
        req.email = "test@test.com";
        assertThrows(ConflictException.class, () -> authService.signup(req));
    }

    @Test
    public void testVerifyUserNotFound() {
        Mockito.when(User.findByEmail(anyString())).thenReturn(null);
        VerifyRequest req = new VerifyRequest();
        req.email = "test@test.com";
        assertThrows(NotFoundException.class, () -> authService.verify(req));
    }

    @Test
    public void testVerifyAlreadyVerified() {
        User user = new User();
        user.isVerified = true;
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);

        VerifyRequest req = new VerifyRequest();
        req.email = "test@test.com";
        assertThrows(BadRequestException.class, () -> authService.verify(req));
    }

    @Test
    public void testVerifyInvalidOTP() {
        User user = new User();
        user.isVerified = false;
        user.otp = "123456";
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);

        VerifyRequest req = new VerifyRequest();
        req.email = "test@test.com";
        req.otp = "654321";
        assertThrows(BadRequestException.class, () -> authService.verify(req));
        
        req.otp = null;
        assertThrows(BadRequestException.class, () -> authService.verify(req));
    }

    @Test
    public void testVerifyInvalidOTPUserOTPNull() {
        User user = new User();
        user.isVerified = false;
        user.otp = null;
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);

        VerifyRequest req = new VerifyRequest();
        req.email = "test@test.com";
        req.otp = "123456";
        assertThrows(BadRequestException.class, () -> authService.verify(req));
    }

    @Test
    public void testVerifyExpiredOTP() {
        User user = new User();
        user.isVerified = false;
        user.otp = "123456";
        user.otpExpiry = LocalDateTime.now().minusMinutes(1);
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);

        VerifyRequest req = new VerifyRequest();
        req.email = "test@test.com";
        req.otp = "123456";
        assertThrows(BadRequestException.class, () -> authService.verify(req));
    }

    @Test
    public void testVerifySuccess() {
        User user = new User();
        user.isVerified = false;
        user.otp = "123456";
        user.otpExpiry = LocalDateTime.now().plusMinutes(10);
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);

        VerifyRequest req = new VerifyRequest();
        req.email = "test@test.com";
        req.otp = "123456";
        
        assertDoesNotThrow(() -> authService.verify(req));
        assertTrue(user.isVerified);
        assertNull(user.otp);
        assertNull(user.otpExpiry);
    }

    @Test
    public void testLoginSuccess() {
        User user = new User();
        user.email = "test@test.com";
        user.password = "hashed_pass";
        user.isVerified = true;
        user.role = "USER";
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);
        Mockito.when(passwordUtils.checkPassword("pass", "hashed_pass")).thenReturn(true);
        Mockito.when(tokenService.generateToken(user)).thenReturn("token123");

        org.acme.payload.request.LoginRequest req = new org.acme.payload.request.LoginRequest();
        req.email = "test@test.com";
        req.password = "pass";

        org.acme.payload.response.AuthResponse resp = authService.login(req);
        assertEquals("token123", resp.token);
    }

    @Test
    public void testLoginInvalidCredentials() {
        Mockito.when(User.findByEmail(anyString())).thenReturn(null);
        org.acme.payload.request.LoginRequest req = new org.acme.payload.request.LoginRequest();
        req.email = "test@test.com";
        req.password = "pass";
        assertThrows(BadRequestException.class, () -> authService.login(req));

        User user = new User();
        user.password = "hashed_pass";
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);
        Mockito.when(passwordUtils.checkPassword("wrong", "hashed_pass")).thenReturn(false);
        req.password = "wrong";
        assertThrows(BadRequestException.class, () -> authService.login(req));
    }

    @Test
    public void testLoginNotVerified() {
        User user = new User();
        user.password = "hashed_pass";
        user.isVerified = false;
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);
        Mockito.when(passwordUtils.checkPassword("pass", "hashed_pass")).thenReturn(true);

        org.acme.payload.request.LoginRequest req = new org.acme.payload.request.LoginRequest();
        req.email = "test@test.com";
        req.password = "pass";
        assertThrows(BadRequestException.class, () -> authService.login(req));
    }

    @Test
    public void testRequestPasswordResetUserNotFound() {
        Mockito.when(User.findByEmail(anyString())).thenReturn(null);
        assertThrows(NotFoundException.class, () -> authService.requestPasswordReset("test@test.com"));
    }

    @Test
    public void testRequestPasswordResetSuccess() {
        User user = new User();
        user.email = "test@test.com";
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);

        assertDoesNotThrow(() -> authService.requestPasswordReset("test@test.com"));
        assertNotNull(user.otp);
        assertNotNull(user.otpExpiry);
    }

    @Test
    public void testResetPasswordUserNotFoundOrInvalidOTP() {
        Mockito.when(User.findByEmail("notfound@test.com")).thenReturn(null);
        assertThrows(BadRequestException.class, () -> authService.resetPassword("notfound@test.com", "123456", "newpass"));

        User user = new User();
        user.otp = "123456";
        Mockito.when(User.findByEmail("invalidotp@test.com")).thenReturn(user);
        assertThrows(BadRequestException.class, () -> authService.resetPassword("invalidotp@test.com", "654321", "newpass"));

        User userNullOtp = new User();
        userNullOtp.otp = null;
        Mockito.when(User.findByEmail("nullotp@test.com")).thenReturn(userNullOtp);
        assertThrows(BadRequestException.class, () -> authService.resetPassword("nullotp@test.com", "123456", "newpass"));
    }

    @Test
    public void testResetPasswordExpiredOTP() {
        User user = new User();
        user.otp = "123456";
        user.otpExpiry = LocalDateTime.now().minusMinutes(1);
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);

        assertThrows(BadRequestException.class, () -> authService.resetPassword("test@test.com", "123456", "newpass"));
    }

    @Test
    public void testResetPasswordSuccess() {
        User user = new User();
        user.otp = "123456";
        user.otpExpiry = LocalDateTime.now().plusMinutes(10);
        Mockito.when(User.findByEmail(anyString())).thenReturn(user);
        Mockito.when(passwordUtils.hashPassword("newpass")).thenReturn("hashed_newpass");

        assertDoesNotThrow(() -> authService.resetPassword("test@test.com", "123456", "newpass"));
        assertEquals("hashed_newpass", user.password);
        assertNull(user.otp);
        assertNull(user.otpExpiry);
    }
}
