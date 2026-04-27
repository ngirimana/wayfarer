package org.acme.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SignupRequest {
    @NotBlank
    @Email
    public String email;

    @NotBlank
    public String phone;

    @NotBlank
    public String password;
}
