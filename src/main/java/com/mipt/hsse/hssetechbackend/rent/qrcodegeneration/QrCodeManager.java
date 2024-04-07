package com.mipt.hsse.hssetechbackend.rent.qrcodegeneration;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.nio.charset.Charset;

public class QrCodeManager {
  public static BitMatrix createQR(String data, int height, int width)
      throws WriterException {
    return new MultiFormatWriter()
        .encode(
            new String(data.getBytes(Charset.defaultCharset()), Charset.defaultCharset()),
            BarcodeFormat.QR_CODE,
            width,
            height);
  }
}
