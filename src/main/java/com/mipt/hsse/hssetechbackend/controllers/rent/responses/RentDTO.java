package com.mipt.hsse.hssetechbackend.controllers.rent.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.controllers.users.responses.GetHumanUserResponse;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import java.time.Instant;
import java.util.UUID;

public record RentDTO(
    @JsonProperty("id") UUID rentId,
    @JsonProperty("item") GetItemResponse item,
    @JsonProperty("renter") GetHumanUserResponse renter,
    @JsonProperty("start") Instant plannedStart,
    @JsonProperty("end") Instant plannedEnd,
    @JsonProperty("fact_start") Instant factStart,
    @JsonProperty("fact_end") Instant factEnd) {
  public RentDTO(Rent rent) {
    this(
        rent.getId(),
        new GetItemResponse(rent.getItem()),
        new GetHumanUserResponse(rent.getRenter()),
        rent.getPlannedStart(),
        rent.getPlannedEnd(),
        rent.getFactStart(),
        rent.getFactEnd());
  }
}
