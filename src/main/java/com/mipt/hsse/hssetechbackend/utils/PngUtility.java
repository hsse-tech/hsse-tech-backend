package com.mipt.hsse.hssetechbackend.utils;

public class PngUtility {
  private static final byte[] PngFormatSignature =
      new byte[] {
        (byte) 0x89,
        (byte) 0x50,
        (byte) 0x4E,
        (byte) 0x47,
        (byte) 0x0D,
        (byte) 0x0A,
        (byte) 0x1A,
        (byte) 0x0A
      };

  public static boolean isPngFormat(byte[] photoBytes) {
    if (photoBytes.length < 8) return false;

    for (int i = 0; i < PngFormatSignature.length; i++)
      if (photoBytes[i] != PngFormatSignature[i])
        return false;
    return true;
  }

  public static byte[] getPngSignature() {
    return PngFormatSignature;
  }
}
