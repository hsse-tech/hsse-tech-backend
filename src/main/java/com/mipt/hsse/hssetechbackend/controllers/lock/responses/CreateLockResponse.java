package com.mipt.hsse.hssetechbackend.controllers.lock.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateLockResponse(@JsonProperty("id") @NotNull UUID id) {}
