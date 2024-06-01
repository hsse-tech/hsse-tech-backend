package com.mipt.hsse.hssetechbackend.payments.providers.events;

public record MerchantNotification(int amount, String orderId, boolean success, String status) {}
