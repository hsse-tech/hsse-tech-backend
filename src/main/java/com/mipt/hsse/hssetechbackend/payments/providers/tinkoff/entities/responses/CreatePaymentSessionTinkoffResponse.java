package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatePaymentSessionTinkoffResponse(
        @JsonProperty("Success") boolean success,
        @JsonProperty("ErrorCode") int errorCode,
        @JsonProperty("TerminalKey") String terminalKey,
        @JsonProperty("Status") String status,
        @JsonProperty("PaymentId") long paymentId,
        @JsonProperty("OrderId") String orderId,
        @JsonProperty("Amount") int amount,
        @JsonProperty("PaymentURL") String paymentUrl) { }
