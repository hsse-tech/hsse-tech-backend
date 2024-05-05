package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Помечает надобность подписи запроса
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TinkoffSign {}
