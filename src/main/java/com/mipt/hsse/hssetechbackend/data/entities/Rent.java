package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "rent")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rent {
  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "\"from\"", nullable = false)
  private Instant startAt;

  @Column(name = "\"to\"", nullable = false)
  private Instant endedAt;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User renter;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column(name = "ended_at")
  private Instant factEndedAt;

  public Rent(Instant startAt, Instant endedAt, User renter, Item item) {
    this.startAt = startAt;
    this.endedAt = endedAt;
    this.item = item;
    this.renter = renter;
  }

  public Rent(Instant startAt, Instant endedAt, User renter, Item item, Instant factEndedAt) {
    this(startAt, endedAt, renter, item);
    this.factEndedAt = factEndedAt;
  }
}
