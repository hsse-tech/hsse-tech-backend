package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "item_type")
public class ItemType {
  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "cost", nullable = false, precision = 9, scale = 2)
  private BigDecimal cost;

  @Column(name = "display_name", nullable = false, length = Integer.MAX_VALUE)
  private String displayName;

  @Column(name = "max_rent_time_minutes")
  private Integer maxRentTimeMinutes;

  @Column(name = "is_photo_required_on_finish")
  private boolean isPhotoRequiredOnFinish;


  public boolean isPhotoRequiredOnFinish() {
    return isPhotoRequiredOnFinish;
  }

  public boolean isPaymentRequired() {
    return cost.compareTo(BigDecimal.ZERO) > 0;
  }
}
