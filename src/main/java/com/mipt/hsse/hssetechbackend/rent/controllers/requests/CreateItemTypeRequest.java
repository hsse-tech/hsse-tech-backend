package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

public record CreateItemTypeRequest(
    String name,
    double cost,
    boolean isPhotoConfirmationRequired,
    int maxRentTime,
    boolean hasLock) {}
