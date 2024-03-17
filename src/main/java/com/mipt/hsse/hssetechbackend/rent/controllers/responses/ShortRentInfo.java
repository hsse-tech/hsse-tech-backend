package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import java.time.Instant;
import java.util.UUID;

public record ShortRentInfo(UUID id, User renter, Instant startTime, Instant endTime) {
  public static ShortRentInfo GetFromRent(Rent rent) {
    return new ShortRentInfo(rent.getId(), rent.getRenter(), rent.getFrom(), rent.getTo());
  }
}
