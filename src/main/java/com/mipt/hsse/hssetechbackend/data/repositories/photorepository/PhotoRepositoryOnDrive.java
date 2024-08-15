package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
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

  @Override
  public byte[] findPhoto(PhotoType photoType, UUID id) throws IOException {
    Path path = getPhotoPath(photoType, id);

    if (!Files.exists(path)) {
      throw new PhotoNotFoundException();
    }

    return Files.readAllBytes(path);
  }

  @Override
  public void save(PhotoType photoType, UUID id, byte[] photoBytes)
      throws IOException, NoSuchAlgorithmException {
    Path path = getPhotoPath(photoType, id);

    if (Files.exists(path)) {
      Files.delete(path);
    }

    Files.createDirectories(path.getParent());
    Files.write(path, photoBytes);
  }

  @Override
  public void deletePhoto(PhotoType photoType, UUID id) throws IOException {
    Path path = getPhotoPath(photoType, id);

    if (Files.exists(path)) {
      Files.delete(path);
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
