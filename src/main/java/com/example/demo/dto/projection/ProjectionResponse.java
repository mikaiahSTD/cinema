package com.example.demo.dto.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProjectionResponse(
    UUID id, Instant datetime, BigDecimal seatPrice, UUID roomId, UUID movieId) {}
