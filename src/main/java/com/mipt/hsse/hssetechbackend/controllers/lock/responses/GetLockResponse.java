package com.mipt.hsse.hssetechbackend.controllers.lock.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import java.util.List;
import java.util.UUID;

public record GetLockResponse(
    @JsonProperty("id") UUID uuid, @JsonProperty("locked_items_ids") List<UUID> lockedItemsIds) {
  public static GetLockResponse fromLock(LockPassport lock) {
    return new GetLockResponse(lock.getId(), lock.getLockedItems().stream().map(Item::getId).toList());
  }
}
