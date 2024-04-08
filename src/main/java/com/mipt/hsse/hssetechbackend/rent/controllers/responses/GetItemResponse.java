package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class GetItemResponse {
  @JsonProperty("item_id")
  @NotNull
  private final UUID itemId;

  @JsonProperty("type_id")
  @NotNull
  private final UUID typeId;

  @JsonProperty("display_name")
  @NotNull
  @NotEmpty
  private final String displayName;

  @JsonProperty("rent_info")
  private final List<GetRentResponse> rents;

  @JsonCreator
  public GetItemResponse(UUID itemId, UUID typeId, String displayName, List<GetRentResponse> rents) {
    this.itemId = itemId;
    this.typeId = typeId;
    this.displayName = displayName;
    this.rents = rents;
  }

  public GetItemResponse(Item item, List<GetRentResponse> rents) {
    itemId = item.getId();
    typeId = item.getType().getId();
    displayName = item.getDisplayName();
    this.rents = rents;
  }

  public GetItemResponse(Item item) {
    this(item, null);
  }
}
