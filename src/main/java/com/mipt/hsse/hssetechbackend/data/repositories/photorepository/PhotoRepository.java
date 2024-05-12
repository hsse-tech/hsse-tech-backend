package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import lombok.Getter;

public interface PhotoRepository {
  @Getter
  enum PhotoType {
    RENT_CONFIRMATION("rent-confirmation"),
    ITEM_THUMBNAIL("item-thumbnail");

    private final String folderName;
    PhotoType(String folderName) {
      this.folderName = folderName;
    }
  }

  boolean existsPhoto(PhotoType photoType, UUID id);

  byte[] findPhoto(PhotoType photoType, UUID rentId) throws IOException;

  void save(PhotoType photoType, UUID rentId, byte[] photoBytes) throws IOException, NoSuchAlgorithmException;

  void deletePhoto(PhotoType photoType, UUID id) throws IOException;
}
