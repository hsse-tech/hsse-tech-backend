package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record CreateItemRequest(
    @JsonProperty("display_name") String displayName,
    @JsonProperty("item_type_id") UUID itemTypeId) {}
