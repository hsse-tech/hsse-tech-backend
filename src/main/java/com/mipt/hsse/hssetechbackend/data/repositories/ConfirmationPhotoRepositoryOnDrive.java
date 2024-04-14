package com.mipt.hsse.hssetechbackend.data.repositories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class ConfirmationPhotoRepositoryOnDrive implements ConfirmationPhotoRepository {
  private static final int UUID_LENGTH = 32;
  private static final int PATH_PART_LENGTH = 8;
  private static final String BASE_PATH = System.getenv("PHOTO_CONFIRMATION_PATH") + "/";

  @Override
  public boolean existsPhotoForRent(UUID rentId) {
    Path path = getFilePathByRentId(rentId);
    return Files.exists(path);
  }

  @Override
  public byte[] getPhotoForRent(UUID rentId) throws IOException {
    Path path = getFilePathByRentId(rentId);
    return Files.readAllBytes(path);
  }

  @Override
  public void save(UUID rentId, byte[] photoBytes) throws IOException, NoSuchAlgorithmException {
    Path path = getFilePathByRentId(rentId);
    Files.createDirectories(path.getParent());

    if (Files.exists(path)) {
      throw new UnsupportedOperationException();
    }

    Files.write(path, photoBytes);
  }

  private Path getFilePathByRentId(UUID rentId) {
    String uuidString = rentId.toString();
    String[] parts = splitUUID(uuidString);
    String filePath = constructFilePath(parts, uuidString);

    return Paths.get(filePath);
  }

  private String[] splitUUID(String uuidString) {
    uuidString = uuidString.replace("-", "");

    String[] parts = new String[(int) Math.ceil(1f * UUID_LENGTH / PATH_PART_LENGTH)];
    for (int i = 0; i < parts.length; i++) {
      parts[i] =
          uuidString.substring(PATH_PART_LENGTH * i, Math.min(uuidString.length(), PATH_PART_LENGTH * (i + 1)));
    }
    return parts;
  }

  private String constructFilePath(String[] parts, String uuidString) {
    StringBuilder filePathBuilder = new StringBuilder(BASE_PATH);

    for (String part : parts) {
      filePathBuilder.append(part).append("/");
    }
    filePathBuilder.deleteCharAt(filePathBuilder.length() - 1);

    filePathBuilder.append("/confirm_image_").append(uuidString);

    return filePathBuilder.toString();
  }
}
