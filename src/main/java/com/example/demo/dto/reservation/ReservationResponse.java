package com.example.demo.dto.reservation;

import com.example.demo.constant.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReservationResponse(
    UUID id,
    Instant createdAt,
    ReservationStatus status,
    UUID projectionId,
    UUID userId,
    List<UUID> seatIds) {}
