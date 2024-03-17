package com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs;

import java.time.Instant;
import java.util.UUID;

public record EditRentTimeRequest(
    UUID itemId, Instant newStartTime, Instant newEndTime) {}
