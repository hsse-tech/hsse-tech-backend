package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PinPhotoConfirmationRequest(@JsonProperty("photo_id") long photoId) {}
