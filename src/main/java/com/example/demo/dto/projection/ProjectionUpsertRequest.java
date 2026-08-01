package com.example.demo.dto.projection;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProjectionUpsertRequest(
    UUID id,
    @NotNull(message = "Datetime is required") Instant datetime,
    @NotNull(message = "Seat price is required")
        @DecimalMin(value = "0", message = "Seat price must be greater than or equal to 0")
        BigDecimal seatPrice,
    @NotNull(message = "Room id is required") UUID roomId,
    @NotNull(message = "Movie id is required") UUID movieId) {}
