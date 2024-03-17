package com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import java.time.Instant;
import java.util.UUID;

public record ShortRentInfo(UUID id, User renter, Instant startTime, Instant endTime) {
  public ShortRentInfo(Rent rent) {
    this(rent.getId(), rent.getRenter(), rent.getFrom(), rent.getTo());
  }
}
