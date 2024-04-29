package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class RequestsSigner implements RequestsSignerBase {
  private final String terminalKey;
  private final TinkoffPropsSerializer propsSerializer;

  public RequestsSigner(
          @Value("#{environment.getProperty('TINKOFF_TERMINAL_KEY')}") String terminalKey,
          TinkoffPropsSerializer propsSerializer) {
    this.terminalKey = terminalKey;
    this.propsSerializer = propsSerializer;
  }

  /**
   * Подписывает запрос:
   * <li>Добавляет TerminalKey</li>
   * <li>Создает токен на основе алгоритма, представленного в <a href="https://www.tinkoff.ru/kassa/dev/payments/#section/Podpis-zaprosa">документации</a></li>
   * @param tinkoffRequest Запрос для подписи
   * @param needSha256Sign True, если требуется дополнительная подпись запроса
   */
  @Override
  public void createSign(TinkoffRequestBase tinkoffRequest, boolean needSha256Sign) {
    tinkoffRequest.setTerminalKey(terminalKey);

    if (!needSha256Sign) return;

    var result = propsSerializer.serialize(tinkoffRequest);
    try {
      var sha256 = MessageDigest.getInstance("SHA-256");
      var sha256Bytes = sha256.digest(result.getBytes(StandardCharsets.UTF_8));

      tinkoffRequest.setToken(bytesToHex(sha256Bytes));
    } catch (NoSuchAlgorithmException e) {
      // ignore
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuilder hexString = new StringBuilder(2 * hash.length);

    for (byte b : hash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }

    return hexString.toString();
  }
}
