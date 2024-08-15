package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import java.util.UUID;

public interface PhotoRepository {
  enum PhotoType {
    RENT_CONFIRMATION("Rent confirmation"),
    ITEM_THUMBNAIL("Item thumbnail");

    private final String stringRepresentation;

    PhotoType(String stringRepresentation) {
      this.stringRepresentation = stringRepresentation;
    }

    @Override
    public String toString() {
      return stringRepresentation;
    }
  }

  boolean existsPhoto(PhotoType photoType, UUID id);

  byte[] findPhoto(PhotoType photoType, UUID rentId);

  void save(PhotoType photoType, UUID rentId, byte[] photoBytes);

  void deletePhoto(PhotoType photoType, UUID id);
}
