package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class PhotoRepositoryOnDrive implements PhotoRepository {
  private static final int UUID_LENGTH = 32;
  private static final int PATH_PART_LENGTH = 8;
  private static final Logger LOGGER = LoggerFactory.getLogger(PhotoRepositoryOnDrive.class);

  private final PhotoTypePathConfiguration pathConfiguration;

  public PhotoRepositoryOnDrive(PhotoTypePathConfiguration pathConfiguration) {
    this.pathConfiguration = pathConfiguration;
  }

  @Override
  public boolean existsPhoto(PhotoType photoType, UUID id) {
    Path path = getFilePathForPhoto(photoType, id);
    return Files.exists(path);
  }

  @Override
  public byte[] findPhoto(PhotoType photoType, UUID id) throws IOException {
    Path path = getFilePathForPhoto(photoType, id);

    if (!Files.exists(path)) {
      throw new PhotoNotFoundException();
    }

    return Files.readAllBytes(path);
  }

  @Override
  public void save(PhotoType photoType, UUID id, byte[] photoBytes)
      throws IOException, NoSuchAlgorithmException {
    Path path = getFilePathForPhoto(photoType, id);

    if (Files.exists(path)) {
      Files.delete(path);
    }

    Files.createDirectories(path.getParent());
    Files.write(path, photoBytes);
  }

  @Override
  public void deletePhoto(PhotoType photoType, UUID id) throws IOException {
    Path path = getFilePathForPhoto(photoType, id);

    if (Files.exists(path)) {
      Files.delete(path);
    }

    // Clear empty directories up to the original folder
    Path parentPath = path.getParent();
    Path originalPath = pathConfiguration.getFolderForType(photoType);
    while (parentPath != null && !parentPath.equals(originalPath)) {
      try (var stream = Files.list(parentPath)) {
        // If the directory is empty, it is removed
        if (stream.findAny().isEmpty()) Files.delete(parentPath);
        else break;
      } catch (IOException e) {
        LOGGER.warn("Failed to list contents of directory: {}", parentPath, e);
        break;
      }

      parentPath = parentPath.getParent();
    }
  }

  public Path getFilePathForPhoto(PhotoType photoType, UUID id) {
    Path filePath = pathConfiguration.getFolderForType(photoType);

    // Add a sequence of folders representing the UUID split into short parts
    String[] parts = splitUUID(id);
    for (String part : parts) {
      filePath = filePath.resolve(part);
    }

    return filePath;
  }

  /**
   * Splits the 32-letter UUID into parts each, probably, except for the last one, consisting of
   * {@link PhotoRepositoryOnDrive#PATH_PART_LENGTH} letters
   */
  private String[] splitUUID(UUID uuid) {
    String uuidString = uuid.toString();
    uuidString = uuidString.replace("-", "");

    String[] parts = new String[(int) Math.ceil(1f * UUID_LENGTH / PATH_PART_LENGTH)];
    for (int i = 0; i < parts.length; i++) {
      parts[i] =
          uuidString.substring(
              PATH_PART_LENGTH * i, Math.min(uuidString.length(), PATH_PART_LENGTH * (i + 1)));
    }
    return parts;
  }
}
