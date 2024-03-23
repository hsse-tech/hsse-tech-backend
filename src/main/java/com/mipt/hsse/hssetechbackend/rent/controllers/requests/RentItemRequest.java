package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record RentItemRequest(
    UUID userId, UUID itemId, @NotNull Instant startTime, @NotNull Instant endTime) {}
