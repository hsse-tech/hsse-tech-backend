package com.mipt.hsse.hssetechbackend.payments.controllers.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TinkoffEventRequest(@JsonProperty("Amount") int amount,
                                  @JsonProperty("OrderId") String orderId,
                                  @JsonProperty("Success") boolean success,
                                  @JsonProperty("Status") String status) {
}
