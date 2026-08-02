package com.example.demo.dto.reservation;

import com.example.demo.constant.ReservationStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record ReservationUpsertRequest(
    UUID id,
    @NotNull(message = "Projection id is required") UUID projectionId,
    @NotEmpty(message = "Seat ids must not be empty") List<UUID> seatIds,
    ReservationStatus status) {}
