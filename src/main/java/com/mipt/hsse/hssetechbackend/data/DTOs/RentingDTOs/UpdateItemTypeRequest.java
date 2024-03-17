package com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs;


public record UpdateItemTypeRequest(
    String newTypeName, double newCost, boolean isPhotoConfirmationRequired) {}
