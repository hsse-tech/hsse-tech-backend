package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record UpdateItemRequest(@NotNull @JsonProperty("new_display_name") String newDisplayName) {}
