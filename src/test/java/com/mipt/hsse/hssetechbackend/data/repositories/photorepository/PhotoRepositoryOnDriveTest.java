package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PhotoRepositoryOnDriveTest {
  private final PhotoRepository photoRepository = new PhotoRepositoryOnDrive("photos");

  @Test
  public void testCreatePhoto() throws IOException, NoSuchAlgorithmException {
    UUID uuid = UUID.randomUUID();
    byte[] bytes = new byte[] {1, 2, 3};
    photoRepository.save(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid, bytes);

    assertTrue(photoRepository.existsPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));
    assertArrayEquals(bytes, photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

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
    assertArrayEquals(otherBytes, photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, uuid));

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
  public void findNonExistingPhoto() {
    assertThrows(PhotoNotFoundException.class, () -> photoRepository.findPhoto(PhotoRepository.PhotoType.ITEM_THUMBNAIL, UUID.randomUUID()));
  }
}
