package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "rent_finish_photo_confirmations")
public class RentFinishPhotoConfirmation {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private long photoId;

  public UUID getId() {
    return id;
  }

  public long getPhotoId() {
    return photoId;
  }

  public void setPhotoId(long photoId) {
    this.photoId = photoId;
  }
}
