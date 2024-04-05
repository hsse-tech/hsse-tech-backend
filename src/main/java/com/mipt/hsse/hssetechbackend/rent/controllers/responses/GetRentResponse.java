package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import java.time.Instant;
import java.util.UUID;

public record GetRentResponse(UUID id, HumanUserPassport renter, Instant startTime, Instant endTime) {
  public static GetRentResponse getFromRent(Rent rent) {
    return new GetRentResponse(
        rent.getId(), rent.getRenter(), rent.getStartAt(), rent.getEndedAt());
  }
}
