package com.mipt.hsse.hssetechbackend.payments.providers;

/**
 * Задает параметры платежной сессии
 * @param amount размер платежа в копейках
 * @param orderId ID заказа
 */
public record SessionParams(int amount, String orderId) {}
