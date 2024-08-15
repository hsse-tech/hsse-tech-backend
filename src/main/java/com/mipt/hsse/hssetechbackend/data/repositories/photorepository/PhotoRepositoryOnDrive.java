package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Implements storing photos on a local drive.
 *
 * @implNote For performance reasons, the photos are distributed into a 3-level-deep folder
 *     structure. The first level of separation is by photo type. The second and the third levels
 *     are by the 1-2 and 3-4 symbols in the hexadecimal 36-symbols-long representation of the image
 *     UUID.
 */
@Repository
public class PhotoRepositoryOnDrive implements PhotoRepository {
  private final String photosPath;

  public PhotoRepositoryOnDrive(@Value("${photos.path}") String photosPath) {
    this.photosPath = photosPath;
  }

  @Override
  public boolean existsPhoto(PhotoType photoType, UUID id) {
    Path path = getPhotoPath(photoType, id);
    return Files.exists(path);
  }

  /**
   * @throws PhotoNotFoundException photo does not exist
   */
  @Override
  public byte[] findPhoto(PhotoType photoType, UUID id) {
    Path path = getPhotoPath(photoType, id);

    if (!Files.exists(path)) {
      throw new PhotoNotFoundException();
    }

    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @throws PhotoAlreadyExistsException Photo with the same PhotoType and id already exists
   */
  @Override
  public void save(PhotoType photoType, UUID id, byte[] photoBytes) {
    Path path = getPhotoPath(photoType, id);

    if (Files.exists(path)) {
      throw new PhotoAlreadyExistsException();
    }

    try {
      Files.createDirectories(path.getParent());
      Files.write(path, photoBytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void deletePhoto(PhotoType photoType, UUID id) {
    Path path = getPhotoPath(photoType, id);

    if (Files.exists(path)) {
      try {
        Files.delete(path);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Path getPhotoPath(PhotoType photoType, UUID id) {
    Path filePath = Path.of(photosPath, photoType.toString());

    String firstTwoSymbols = id.toString().substring(0, 2);
    String secondTwoSymbols = id.toString().substring(2, 4);

    String[] parts = new String[] {firstTwoSymbols, secondTwoSymbols};
    for (String part : parts) {
      filePath = filePath.resolve(part);
    }

    filePath = filePath.resolve(id + ".png");

    return filePath;
  }
}
