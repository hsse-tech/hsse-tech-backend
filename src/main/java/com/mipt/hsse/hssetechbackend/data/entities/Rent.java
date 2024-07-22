package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "rent")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rent {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "\"from\"", nullable = false)
  private Instant plannedStart;

  @Column(name = "\"to\"", nullable = false)
  private Instant plannedEnd;

  @ManyToOne(optional = false)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column(name = "ended_at")
  private Instant factEnd;

  @Column(name = "started_at")
  private Instant factStart;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "user_id", nullable = false)
  private HumanUserPassport renter;

  public Rent(Instant plannedStart, Instant plannedEnd, HumanUserPassport renter, Item item) {
    this.plannedStart = plannedStart;
    this.plannedEnd = plannedEnd;
    this.item = item;
    this.renter = renter;
  }

  public Rent(
      Instant plannedStart,
      Instant plannedEnd,
      HumanUserPassport renter,
      Item item,
      Instant factEnd) {
    this(plannedStart, plannedEnd, renter, item);
    this.factEnd = factEnd;
  }
}
