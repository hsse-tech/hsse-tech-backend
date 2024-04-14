package com.mipt.hsse.hssetechbackend.data.repositories;


import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public interface ConfirmationPhotoRepository {
  boolean existsPhotoForRent(UUID rentId);

  byte[] getPhotoForRent(UUID rentId) throws IOException;

  void save(UUID rentId, byte[] photoBytes) throws IOException, NoSuchAlgorithmException;
}
