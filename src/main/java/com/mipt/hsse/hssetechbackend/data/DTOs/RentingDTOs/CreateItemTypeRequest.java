package com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs;


public record CreateItemTypeRequest(
    String name,
    double cost,
    boolean isPhotoConfirmationRequired,
    int maxRentTime,
    boolean hasLock) {}

