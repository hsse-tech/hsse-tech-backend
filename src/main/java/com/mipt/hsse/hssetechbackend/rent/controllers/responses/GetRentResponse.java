package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import java.time.Instant;
import java.util.UUID;

public record GetRentResponse(UUID id, User renter, Instant startTime, Instant endTime) {
  public static GetRentResponse getFromRent(Rent rent) {
    return new GetRentResponse(rent.getId(), rent.getRenter(), rent.getStartAt(), rent.getEndedAt());
  }
}
