package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;

@Getter
public class GetItemWithRentsResponse extends GetItemResponse {
  @JsonProperty("rent_info")
  @NotNull
  private final Set<GetRentResponse> rents;
  
  public GetItemWithRentsResponse(
      @NotNull Item item,
      @NotNull Set<GetRentResponse> rents
      ) {
    super(item);
    this.rents = rents;
  }
}
