package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {PhotoRepositoryOnDrive.class})
@TestPropertySource("classpath:application-test.properties")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PhotoRepositoryOnDriveTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(PhotoRepositoryOnDriveTest.class);
  @Autowired private PhotoRepositoryOnDrive photoRepository;

  @AfterAll
  public static void deleteTestImagesFolder(@Value("${photos.path}") String photosFolder) {
    try {
      deleteFile(new File(photosFolder));
    } catch (IOException e) {
      LOGGER.warn("Failed to delete the test images folder after the tests have finished", e);
    }
  }

  // Deletes a file or a directory with all its content recursively
  private static void deleteFile(File file) throws IOException {
    File[] contents = file.listFiles();
    if (contents != null) {
      for (File f : contents) {
        deleteFile(f);
      }
    }

    if (!file.delete()) throw new IOException();
  }

  @Test
  public void testGetPhotoPath(@Value("${photos.path}") String expectedFolder) {
    UUID uuid = UUID.fromString("01234567-89ab-cdef-0123-456789abcdef");
    Path actualPath =
        photoRepository.getPhotoPath(PhotoRepository.PhotoType.RENT_CONFIRMATION, uuid);
    Path expectedPath =
        Path.of(
            expectedFolder,
            PhotoRepository.PhotoType.RENT_CONFIRMATION.toString(),
            "01",
            "23",
            uuid + ".png");
    assertEquals(expectedPath, actualPath);
  }

  @Test
  public void testCreatePhoto() {
    UUID uuid = UUID.randomUUID();
    byte[] bytes = new byte[] {1, 2, 3};
    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, bytes);

    assertTrue(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));
    assertArrayEquals(
        bytes, photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

    photoRepository.deletePhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid);
  }

  @Test
  public void testDeletePhoto() {
    UUID uuid = UUID.randomUUID();

    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, new byte[] {1, 2, 3});
    assertTrue(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

    photoRepository.deletePhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid);
    assertFalse(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));
  }

  @Test
  public void findNonExistingPhoto() {
    assertThrows(
        PhotoNotFoundException.class,
        () ->
            photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, UUID.randomUUID()));
  }
}
