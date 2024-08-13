package com.mipt.hsse.hssetechbackend.data.repositories.photorepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PhotoTypePathConfiguration {
  private final Map<PhotoRepository.PhotoType, Path> photoTypeToFolderPath;

  public PhotoTypePathConfiguration(
      @Value("${photos.paths.rent-confirmation}") String rentConfirmationPhotoPath,
      @Value("${photos.paths.item-thumbnail}") String itemThumbnailPhotoPath) {
    photoTypeToFolderPath = new HashMap<>();
    photoTypeToFolderPath.put(
        PhotoRepository.PhotoType.RENT_CONFIRMATION, Paths.get(rentConfirmationPhotoPath));
    photoTypeToFolderPath.put(
        PhotoRepository.PhotoType.ITEM_THUMBNAIL, Paths.get(itemThumbnailPhotoPath));
  }

  public Path getFolderForType(PhotoRepository.PhotoType photoType) {
    return photoTypeToFolderPath.get(photoType);
  }
}
