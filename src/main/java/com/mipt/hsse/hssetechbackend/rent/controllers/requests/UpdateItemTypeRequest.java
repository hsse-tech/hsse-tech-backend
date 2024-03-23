package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

/**
 * Request to update item type<br>
 * Note that not-nullability of fields is not required: fields left null will not be updated in the
 * target item type object
 */
public record UpdateItemTypeRequest(
    @JsonProperty("new_display_name") String newDisplayName,
    @PositiveOrZero @JsonProperty("new_cost") BigDecimal newCost,
    @JsonProperty("is_photo_confirmation_required") Boolean isPhotoConfirmationRequired,
    @Positive @JsonProperty("max_rent_time_minutes") Integer newMaxRentTimeMinutes) {}
