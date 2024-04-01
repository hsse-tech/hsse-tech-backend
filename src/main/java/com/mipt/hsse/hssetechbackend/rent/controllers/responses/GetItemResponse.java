package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
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
  
  public GetItemResponse(Item item) {
    itemId = item.getId();
    typeId = item.getType().getId();
    displayName = item.getDisplayName();
  }
}
