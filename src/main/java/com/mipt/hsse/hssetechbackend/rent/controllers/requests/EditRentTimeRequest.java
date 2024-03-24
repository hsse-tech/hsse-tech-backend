package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

public record EditRentTimeRequest(
    @JsonProperty("item_") UUID itemId,
    @JsonProperty("new_time_start") Instant newStartTime,
    @JsonProperty("new_time_end") Instant newEndTime) {}
