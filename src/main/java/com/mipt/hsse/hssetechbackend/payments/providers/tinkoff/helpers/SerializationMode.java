package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers;

public enum SerializationMode {
  /**
   * Режим, при котором тело запроса остается неизменным
   */
  DEFAULT,

  /**
   * Режим, при котором запрос подписывается SHA256 хэшем-подписью
   * (в запрос добавляется поле <code>Token:String</code>, которое рассчитывается по <a href="https://www.tinkoff.ru/kassa/dev/payments/#section/Podpis-zaprosa">этому</a> алгоритму)
   */
  SIGN_SHA256_TOKEN
}
