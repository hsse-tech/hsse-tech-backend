package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mipt.hsse.hssetechbackend.auxiliary.serializablebytesarray.BytesArray;

public record PinPhotoConfirmationRequest(@JsonProperty("photo") BytesArray photoBytesArray) {}
