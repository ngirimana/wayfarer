package org.acme.service;

import org.acme.payload.request.LoginRequest;
import org.acme.payload.request.SignupRequest;
import org.acme.payload.request.VerifyRequest;
import org.acme.payload.response.AuthResponse;

public interface AuthService {
    void signup(SignupRequest request);
    void verify(VerifyRequest request);
    AuthResponse login(LoginRequest request);
    void requestPasswordReset(String email);
    void resetPassword(String email, String otp, String newPassword);
}
