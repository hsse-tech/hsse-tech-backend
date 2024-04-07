package com.mipt.hsse.hssetechbackend.rent.controllers.responses;

public record RentInfoResponse(
    long itemId, long itemTypeId, String displayName, GetShortRentResponse rent) {}
