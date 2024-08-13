package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;


import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Repository
public interface PhotoRepository {
  enum PhotoType {
    RENT_CONFIRMATION,
    ITEM_THUMBNAIL
  }

  boolean existsPhoto(PhotoType photoType, UUID id);

  byte[] findPhoto(PhotoType photoType, UUID rentId) throws IOException;

  void save(PhotoType photoType, UUID rentId, byte[] photoBytes)
      throws IOException, NoSuchAlgorithmException;

  void deletePhoto(PhotoType photoType, UUID id) throws IOException;
}
