package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

/**
 * Request to update item<br>
 * Note that not-nullability of fields is not required: fields left null will not be updated in the
 * target item type object
 */
public record UpdateItemRequest(@JsonProperty("new_display_name") @NotNull String newDisplayName) {}
