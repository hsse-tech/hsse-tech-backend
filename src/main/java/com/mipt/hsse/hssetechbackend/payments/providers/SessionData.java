package com.mipt.hsse.hssetechbackend.payments.providers;

public record SessionData(int amount, String orderId, boolean isSuccess, String paymentUrl) {}
