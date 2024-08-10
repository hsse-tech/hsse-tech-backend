package com.mipt.hsse.hssetechbackend.controllers.rent.responses;

public record RentInfoResponse(
    long itemId, long itemTypeId, String displayName, GetShortRentResponse rent) {}
