package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {PhotoRepositoryOnDrive.class, PhotoTypePathConfiguration.class})
@TestPropertySource("classpath:application-test.properties")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PhotoRepositoryOnDriveTest {
  @Autowired private PhotoRepositoryOnDrive photoRepository;

  @Test
  public void testGetFilePathForPhoto(
      @Value("${photos.paths.rent-confirmation}") String expectedFolder) {
    UUID uuid = UUID.randomUUID();
    Path actualPath =
        photoRepository.getFilePathForPhoto(PhotoRepository.PhotoType.RENT_CONFIRMATION, uuid);

    assertTrue(actualPath.startsWith(expectedFolder));
  }

  @Test
  public void testCreatePhoto() throws IOException, NoSuchAlgorithmException {
    UUID uuid = UUID.randomUUID();
    byte[] bytes = new byte[] {1, 2, 3};
    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, bytes);

    assertTrue(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));
    assertArrayEquals(
        bytes, photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

    photoRepository.deletePhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid);
  }

  @Test
  public void testReplacePhoto() throws IOException, NoSuchAlgorithmException {
    UUID uuid = UUID.randomUUID();

    // Create the first photo
    byte[] bytes = new byte[] {1, 2, 3};
    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, bytes);
    assertTrue(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

    // Replace the first photo with another one
    byte[] otherBytes = new byte[] {4, 5, 6};
    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, otherBytes);
    assertTrue(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));
    assertArrayEquals(
        otherBytes, photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

    photoRepository.deletePhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid);
  }

  @Test
  public void testDeletePhoto() throws IOException, NoSuchAlgorithmException {
    UUID uuid = UUID.randomUUID();

    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, new byte[] {1, 2, 3});
    assertTrue(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

    photoRepository.deletePhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid);
    assertFalse(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));
  }

  @Test
  public void testDeletesFolderAfterDeletePhoto(
      @Value("${photos.paths.item-thumbnail}") String folderPath)
      throws IOException, NoSuchAlgorithmException {
    UUID uuid = UUID.randomUUID();

    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, new byte[] {1, 2, 3});
    photoRepository.deletePhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid);

    Path path = photoRepository.getFilePathForPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid);
    Path originalPath = Path.of(folderPath);
    while (path != null && !path.equals(originalPath)) {
      assertFalse(Files.exists(path));
      path = path.getParent();
    }
  }

  @Test
  public void findNonExistingPhoto() {
    assertThrows(
        PhotoNotFoundException.class,
        () ->
            photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, UUID.randomUUID()));
  }
}
