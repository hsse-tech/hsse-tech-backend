package com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public final class ShortRentInfo {
  @NotNull
  private final UUID id;
  @NotNull
  private final User renter;
  @NotNull
  private final Instant startTime;
  @NotNull
  private final Instant endTime;

  public ShortRentInfo(Rent rent) {
    id = rent.getId();
    renter = rent.getRenter();
    startTime = rent.getFrom();
    endTime = rent.getTo();
  }
}
