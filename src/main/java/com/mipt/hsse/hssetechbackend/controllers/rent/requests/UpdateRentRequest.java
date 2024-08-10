package com.mipt.hsse.hssetechbackend.controllers.rent.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record UpdateRentRequest(
    @JsonProperty("new_time_start") @NotNull Instant newStartTime,
    @JsonProperty("new_time_end") @NotNull Instant newEndTime) {}
