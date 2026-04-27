package org.acme.controller.v1;

import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.entity.User;
import org.acme.payload.request.LoginRequest;
import org.acme.payload.request.SignupRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;

import org.acme.security.PasswordUtils;
import org.acme.security.TokenService;
import org.acme.payload.response.AuthResponse;
import io.quarkus.test.InjectMock;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestTransaction
public class AuthControllerTest {

    @InjectMock
    PasswordUtils passwordUtils;

    @InjectMock
    TokenService tokenService;

    // ==================== SIGNUP TESTS ====================

    @Test
    public void testSignupSuccess() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(null);
        when(passwordUtils.hashPassword(anyString())).thenReturn("hashed_password");

        SignupRequest request = new SignupRequest();
        request.email = "new_user_" + System.currentTimeMillis() + "@example.com";
        request.phone = "078" + (System.currentTimeMillis() % 10000000);
        request.password = "password123";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/signup")
          .then()
             .statusCode(201)
             .body("message", is("User registered. Please verify with OTP sent to your email/phone."));
    }

    @Test
    public void testSignupEmailAlreadyExists() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(new User());

        SignupRequest request = new SignupRequest();
        request.email = "existing@example.com";
        request.phone = "0780000001";
        request.password = "password123";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/signup")
          .then()
             .statusCode(409);
    }

    @Test
    public void testSignupMissingEmail() {
        SignupRequest request = new SignupRequest();
        request.phone = "0780000002";
        request.password = "password123";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/signup")
          .then()
             .statusCode(400);
    }

    @Test
    public void testSignupMissingPassword() {
        SignupRequest request = new SignupRequest();
        request.email = "nopwd@example.com";
        request.phone = "0780000003";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/signup")
          .then()
             .statusCode(400);
    }

    @Test
    public void testSignupEmptyBody() {
        given()
          .contentType(ContentType.JSON)
          .body("{}")
          .when().post("/api/v1/auth/signup")
          .then()
             .statusCode(400);
    }

    // ==================== LOGIN TESTS ====================

    @Test
    public void testLoginSuccess() {
        PanacheMock.mock(User.class);
        User mockUser = new User();
        mockUser.email = "login@example.com";
        mockUser.password = "hashed_password";
        mockUser.isVerified = true;
        mockUser.role = "USER";

        Mockito.when(User.findByEmail("login@example.com")).thenReturn(mockUser);
        Mockito.when(passwordUtils.checkPassword(anyString(), anyString())).thenReturn(true);
        Mockito.when(tokenService.generateToken(any())).thenReturn("mocked_token");

        LoginRequest request = new LoginRequest();
        request.email = "login@example.com";
        request.password = "password123";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/login")
          .then()
             .statusCode(200)
             .body("email", is("login@example.com"))
             .body("role", is("USER"))
             .body("token", is("mocked_token"));
    }

    @Test
    public void testLoginUserNotFound() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(null);

        LoginRequest request = new LoginRequest();
        request.email = "nobody@example.com";
        request.password = "password123";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/login")
          .then()
             .statusCode(400);
    }

    @Test
    public void testLoginWrongPassword() {
        PanacheMock.mock(User.class);
        User mockUser = new User();
        mockUser.email = "wrongpwd@example.com";
        mockUser.password = "correct_hash";
        mockUser.isVerified = true;

        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(mockUser);
        Mockito.when(passwordUtils.checkPassword(anyString(), anyString())).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.email = "wrongpwd@example.com";
        request.password = "wrong_password";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/login")
          .then()
             .statusCode(400);
    }

    @Test
    public void testLoginUnverifiedAccount() {
        PanacheMock.mock(User.class);
        User mockUser = new User();
        mockUser.email = "unverified@example.com";
        mockUser.password = "hashed_password";
        mockUser.isVerified = false;

        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(mockUser);
        Mockito.when(passwordUtils.checkPassword(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        LoginRequest request = new LoginRequest();
        request.email = "unverified@example.com";
        request.password = "password123";

        given()
          .contentType(ContentType.JSON)
          .body(request)
          .when().post("/api/v1/auth/login")
          .then()
             .statusCode(400)
             .body("message", is("Account not verified"));
    }

    @Test
    public void testLoginMissingFields() {
        given()
          .contentType(ContentType.JSON)
          .body("{}")
          .when().post("/api/v1/auth/login")
          .then()
             .statusCode(400);
    }

    // ==================== OTP VERIFY TESTS ====================

    @Test
    public void testVerifyOTPSuccess() {
        PanacheMock.mock(User.class);
        User mockUser = new User();
        mockUser.email = "verify@example.com";
        mockUser.otp = "123456";
        mockUser.otpExpiry = LocalDateTime.now().plusMinutes(10);
        mockUser.isVerified = false;

        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(mockUser);

        given()
          .contentType(ContentType.JSON)
          .body("{\"email\":\"verify@example.com\", \"otp\":\"123456\"}")
          .when().post("/api/v1/auth/verify")
          .then()
             .statusCode(200)
             .body("message", is("Account verified successfully"));
    }

    @Test
    public void testVerifyOTPWrongCode() {
        PanacheMock.mock(User.class);
        User mockUser = new User();
        mockUser.email = "verify@example.com";
        mockUser.otp = "123456";
        mockUser.otpExpiry = LocalDateTime.now().plusMinutes(10);

        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(mockUser);

        given()
          .contentType(ContentType.JSON)
          .body("{\"email\":\"verify@example.com\", \"otp\":\"000000\"}")
          .when().post("/api/v1/auth/verify")
          .then()
             .statusCode(400);
    }

    @Test
    public void testVerifyOTPExpired() {
        PanacheMock.mock(User.class);
        User mockUser = new User();
        mockUser.email = "expired@example.com";
        mockUser.otp = "123456";
        mockUser.otpExpiry = LocalDateTime.now().minusMinutes(1);

        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(mockUser);

        given()
          .contentType(ContentType.JSON)
          .body("{\"email\":\"expired@example.com\", \"otp\":\"123456\"}")
          .when().post("/api/v1/auth/verify")
          .then()
             .statusCode(400)
             .body("message", is("OTP expired"));
    }

    @Test
    public void testVerifyOTPUserNotFound() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(null);

        given()
          .contentType(ContentType.JSON)
          .body("{\"email\":\"ghost@example.com\", \"otp\":\"123456\"}")
          .when().post("/api/v1/auth/verify")
          .then()
             .statusCode(404);
    }

    // ==================== PASSWORD RESET TESTS ====================

    @Test
    public void testForgotPasswordSuccess() {
        PanacheMock.mock(User.class);
        User mockUser = new User();
        mockUser.email = "test@example.com";
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(mockUser);

        given()
          .queryParam("email", "test@example.com")
          .when().post("/api/v1/auth/reset-password-request")
          .then()
             .statusCode(200)
             .body("message", is("OTP sent to your email/phone."));
    }

    @Test
    public void testForgotPasswordUserNotFound() {
        PanacheMock.mock(User.class);
        Mockito.when(User.findByEmail(Mockito.anyString())).thenReturn(null);

        given()
          .queryParam("email", "nobody@example.com")
          .when().post("/api/v1/auth/reset-password-request")
          .then()
             .statusCode(404);
    }
}
