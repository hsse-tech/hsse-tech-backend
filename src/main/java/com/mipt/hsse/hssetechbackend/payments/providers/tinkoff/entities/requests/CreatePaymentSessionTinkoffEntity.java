package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests;

public record CreatePaymentSessionTinkoffEntity(int amount, String orderId) {}
