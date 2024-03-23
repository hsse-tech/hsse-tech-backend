package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateItemTypeRequest(
    @NotNull @PositiveOrZero BigDecimal cost,
    @NotNull @NotEmpty String displayName,
    Integer maxRentTimeMinutes,
    boolean isPhotoConfirmationRequired) {
  public ItemType getItemType() {
    return new ItemType(cost, displayName, maxRentTimeMinutes, isPhotoConfirmationRequired);
  }
}
