package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import java.util.UUID;

public record CreateItemRequest(String displayName, UUID itemTypeId) {}
