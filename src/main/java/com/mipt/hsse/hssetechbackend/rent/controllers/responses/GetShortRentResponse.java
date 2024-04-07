package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import java.time.Instant;
import java.util.UUID;

public record GetShortRentResponse(UUID id, HumanUserPassport renter, Instant startTime, Instant endTime) {
  public static GetShortRentResponse getFromRent(Rent rent) {
    return new GetShortRentResponse(
        rent.getId(), rent.getRenter(), rent.getPlannedStart(), rent.getPlannedEnd());
  }
}
