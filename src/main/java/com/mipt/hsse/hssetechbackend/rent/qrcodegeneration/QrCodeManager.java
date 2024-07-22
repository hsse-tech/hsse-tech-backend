package com.mipt.hsse.hssetechbackend.rent.qrcodegeneration;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class QrCodeManager {
  public static BitMatrix createQR(String data, int width, int height) throws WriterException {
    Map<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

    return new MultiFormatWriter()
        .encode(
            data,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints);
  }

  public static String readQR(byte[] qrCodeBytes) throws IOException, NotFoundException {
    BufferedImage qrCodeBufferedImage = ImageIO.read(new ByteArrayInputStream(qrCodeBytes));

    LuminanceSource source = new BufferedImageLuminanceSource(qrCodeBufferedImage);
    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

    Map<DecodeHintType, Object> hints = new HashMap<>();
    hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());

    Result result = new MultiFormatReader().decode(bitmap, hints);
    return result.getText();
  }
}
