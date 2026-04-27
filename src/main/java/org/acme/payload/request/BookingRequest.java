package org.acme.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BookingRequest {
    @NotNull
    public Long tripId;

    @Positive
    public int seatNumber;
}
