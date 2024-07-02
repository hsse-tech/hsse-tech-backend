package com.mipt.hsse.hssetechbackend.data.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "item_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemType {
  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "cost", nullable = false, precision = 9, scale = 2)
  @PositiveOrZero
  @NotNull
  private BigDecimal cost;

  @Column(name = "display_name", nullable = false, length = Integer.MAX_VALUE)
  @NotNull
  @NotEmpty
  @JsonProperty("display_name")
  private String displayName;

  @Column(name = "max_rent_time_minutes")
  @JsonProperty("max_rent_time_minutes")
  @Positive
  private Integer maxRentTimeMinutes;

  @Column(name = "is_photo_required_on_finish")
  @JsonProperty("is_photo_confirmation_required")
  private boolean isPhotoRequiredOnFinish;

  public ItemType(
      BigDecimal cost,
      String displayName,
      Integer maxRentTimeMinutes,
      boolean isPhotoRequiredOnFinish) {
    this.cost = cost;
    this.displayName = displayName;
    this.maxRentTimeMinutes = maxRentTimeMinutes;
    this.isPhotoRequiredOnFinish = isPhotoRequiredOnFinish;
  }

  @JsonIgnore
  public boolean isPaymentRequired() {
    return cost.compareTo(BigDecimal.ZERO) > 0;
  }
}
