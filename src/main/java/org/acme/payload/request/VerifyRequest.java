package org.acme.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyRequest {
    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String otp;
}
