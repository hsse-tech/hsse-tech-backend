package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record CreateItemTypeRequest(
    @JsonProperty("cost") @PositiveOrZero @NotNull BigDecimal cost,
    @JsonProperty("display_name") @NotNull @NotEmpty String displayName,
    @JsonProperty("max_rent_time_minutes") @Positive  Integer maxRentTimeMinutes,
    @JsonProperty("is_photo_confirmation_required") boolean isPhotoConfirmationRequired) {}
