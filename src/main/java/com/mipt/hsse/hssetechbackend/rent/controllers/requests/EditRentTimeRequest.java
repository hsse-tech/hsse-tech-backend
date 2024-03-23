package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import java.time.Instant;
import java.util.UUID;

public record EditRentTimeRequest(UUID itemId, Instant newStartTime, Instant newEndTime) {}
