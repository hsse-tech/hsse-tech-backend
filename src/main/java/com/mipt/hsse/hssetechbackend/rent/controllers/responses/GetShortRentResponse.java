package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class GetShortRentResponse {
  @JsonProperty("id")
  private final UUID id;

  @JsonProperty("renter")
  private final HumanUserPassport renter;

  @JsonProperty("start_time")
  private final Instant startTime;

  @JsonProperty("end_time")
  private final Instant endTime;

  @JsonCreator
  public GetShortRentResponse(
      @JsonProperty("id") UUID id,
      @JsonProperty("renter") HumanUserPassport renter,
      @JsonProperty("start_time") Instant startTime,
      @JsonProperty("end_time") Instant endTime) {
    this.id = id;
    this.renter = renter;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public static GetShortRentResponse getFromRent(Rent rent) {
    return new GetShortRentResponse(
        rent.getId(), rent.getRenter(), rent.getPlannedStart(), rent.getPlannedEnd());
  }
}
