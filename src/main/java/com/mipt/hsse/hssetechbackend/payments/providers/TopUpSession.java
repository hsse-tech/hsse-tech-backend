package com.mipt.hsse.hssetechbackend.payments.providers;

public record TopUpSession(boolean successfullyCreated, String paymentUrl) {}
