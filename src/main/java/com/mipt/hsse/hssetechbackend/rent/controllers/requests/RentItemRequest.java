package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record RentItemRequest(
    @JsonProperty("user_id") UUID userId,
    @JsonProperty("item_id") UUID itemId,
    @JsonProperty("start_time") @NotNull Instant startTime,
    @JsonProperty("end_time") @NotNull Instant endTime) {}
