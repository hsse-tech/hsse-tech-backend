package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rent_finish_photo_confirmation")
public class RentFinishPhotoConfirmation {
  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private long photoId;
}
