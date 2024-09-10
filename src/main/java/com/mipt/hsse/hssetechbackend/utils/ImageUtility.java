package com.mipt.hsse.hssetechbackend.utils;

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

  private static final int SIGNATURE_LENGTH = 4;

  public static boolean isAllowedImageFormat(byte[] imageBytes) {
    return getAllowedFormat(imageBytes) != null;
  }

  public static String getFormatExtension(byte[] imageBytes) {
    var format = getAllowedFormat(imageBytes);
    if (format != null) return format.extension;
    else throw new IllegalArgumentException();
  }

  private static ImageFormat getAllowedFormat(byte[] imageBytes) {
    if (imageBytes.length < SIGNATURE_LENGTH) return null;

    byte[] imageSignature = new byte[] {imageBytes[0], imageBytes[1], imageBytes[2], imageBytes[3]};

    for (ImageFormat format : ImageFormat.values()) {
      boolean isFormat = true;
      // Compare the first 4 bytes of the image with signatures
      for (int i = 0; i < SIGNATURE_LENGTH; i++) {
        if (imageSignature[i] != format.signature[i]) {
          isFormat = false;
          break;
        }
      }

      if (isFormat) {
        return format;
      }
    }

    return null;
  }
}
