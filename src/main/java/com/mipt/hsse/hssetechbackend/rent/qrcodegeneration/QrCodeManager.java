package com.mipt.hsse.hssetechbackend.rent.qrcodegeneration;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;

public class QrCodeManager {
  @Value("${item-qrcode-width}")
  private static int width;
  @Value("${item-qrcode-height}")
  private static int height;

  public static byte[] createQRByteArray(String data)
      throws WriterException, IOException {
    BufferedImage bufferedImage = createQRBufferedImage(data);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }

  public static BufferedImage createQRBufferedImage(String data)
      throws WriterException {
    BitMatrix qrCodeBitMatrix = createQR(data);
    return MatrixToImageWriter.toBufferedImage(qrCodeBitMatrix);
  }

  public static BitMatrix createQR(String data) throws WriterException {
    return new MultiFormatWriter()
        .encode(
            new String(data.getBytes(Charset.defaultCharset()), Charset.defaultCharset()),
            BarcodeFormat.QR_CODE,
            width,
            height);
  }
}
