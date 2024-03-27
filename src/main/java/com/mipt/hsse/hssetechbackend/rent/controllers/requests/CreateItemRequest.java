package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateItemRequest(
    @JsonProperty("display_name") @NotNull String displayName,
    @JsonProperty("item_type_id") @NotNull UUID itemTypeId) {}