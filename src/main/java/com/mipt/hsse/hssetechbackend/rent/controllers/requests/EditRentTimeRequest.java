package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record EditRentTimeRequest(
    @JsonProperty("item_id") @NotNull UUID itemId,
    @JsonProperty("new_time_start") @NotNull Instant newStartTime,
    @JsonProperty("new_time_end") @NotNull Instant newEndTime) {}
