package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

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
  private Instant from;

  @Column(name = "\"to\"", nullable = false)
  private Instant to;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User renter;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column(name = "ended_at")
  private Instant endedAt;

  public Rent(Instant from, Instant to, User renter, Item item) {
    this.from = from;
    this.to = to;
    this.item = item;
    this.renter = renter;
  }

  public Rent(Instant from, Instant to, User renter, Item item, Instant endedAt) {
    this(from, to, renter, item);
    this.endedAt = endedAt;
  }
}
