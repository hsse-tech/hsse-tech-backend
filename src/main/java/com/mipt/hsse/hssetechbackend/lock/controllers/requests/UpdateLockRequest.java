package com.mipt.hsse.hssetechbackend.lock.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateLockRequest(@JsonProperty("new_item_id") @NotNull UUID itemId) {}
