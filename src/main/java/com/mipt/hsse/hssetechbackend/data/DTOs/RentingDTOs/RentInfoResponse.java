package com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs;

public record RentInfoResponse(
    long itemId, long itemTypeId, String displayName, ShortRentInfo rent) {}
