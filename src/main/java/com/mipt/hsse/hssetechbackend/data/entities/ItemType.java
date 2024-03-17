package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "item_type")
public class ItemType {
  @Id
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
  
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Integer getMaxRentTimeMinutes() {
    return maxRentTimeMinutes;
  }

  public void setMaxRentTimeMinutes(Integer maxRentTimeMinutes) {
    this.maxRentTimeMinutes = maxRentTimeMinutes;
  }

  public boolean isPhotoRequiredOnFinish() {
    return isPhotoRequiredOnFinish;
  }

  public boolean isPaymentRequired() {
    return cost.compareTo(BigDecimal.ZERO) > 0;
  }
}
