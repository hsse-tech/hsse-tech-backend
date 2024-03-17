package com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs;

import java.util.UUID;

public record CreateItemRequest(UUID typeId, String displayName) {}
