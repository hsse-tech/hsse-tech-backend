package com.mipt.hsse.hssetechbackend.utils;

import java.util.Arrays;

public class ImageUtility {
  private enum ImageFormat {
    PNG("png", new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47}),
    JPG("jpg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xdb}),
    JPEG("jpeg", new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0});

    public final String extension;
    public final byte[] signature;

    ImageFormat(String extension, byte[] signature) {
      this.extension = extension;
      this.signature = signature;
    }
  }

  public static boolean isAllowedImageFormat(byte[] imageBytes) {
    return getAllowedFormat(imageBytes) != null;
  }

  public static String getFormatExtension(byte[] imageBytes) {
    var format = getAllowedFormat(imageBytes);
    if (format != null) return format.extension;
    else throw new IllegalArgumentException();
  }

  private static ImageFormat getAllowedFormat(byte[] imageBytes) {
    if (imageBytes.length < 4) return null;

    byte[] imageSignature = new byte[] {imageBytes[0], imageBytes[1], imageBytes[2], imageBytes[3]};

    for (ImageFormat format : ImageFormat.values()) {
      if (Arrays.equals(imageSignature, format.signature)) {
        return format;
      }
    }

    return null;
  }
}
