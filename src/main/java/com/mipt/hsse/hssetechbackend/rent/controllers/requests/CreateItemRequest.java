package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import java.util.UUID;

public record CreateItemRequest(UUID typeId, String displayName) {}
