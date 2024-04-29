package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import org.springframework.beans.factory.annotation.Value;

public class RequestsSigner {
  private final String terminalKey;
  private final String password;

  public RequestsSigner(
          @Value("#{environment.getProperty('TINKOFF_TERMINAL_KEY')}") String terminalKey,
          @Value("#{environment.getProperty('TINKOFF_PASSWORD')}") String password) {
    this.terminalKey = terminalKey;
    this.password = password;
  }

  /**
   * Подписывает запрос:
   * <li>Добавляет TerminalKey</li>
   * <li>Создает токен на основе алгоритма, представленного в <a href="https://www.tinkoff.ru/kassa/dev/payments/#section/Podpis-zaprosa">документации</a></li>
   * @param tinkoffRequest Запрос для подписи
   * @param needSha256Sign True, если требуется дополнительная подпись запроса
   */
  public void createSign(TinkoffRequestBase tinkoffRequest, boolean needSha256Sign) {
    tinkoffRequest.setTerminalKey(terminalKey);
  }
}
