package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Помечает надобность подписи запроса
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TinkoffSign {}
