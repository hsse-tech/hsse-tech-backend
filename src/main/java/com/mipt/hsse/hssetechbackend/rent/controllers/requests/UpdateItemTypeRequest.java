package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

public record UpdateItemTypeRequest(
    String newTypeName, double newCost, boolean isPhotoConfirmationRequired) {}
