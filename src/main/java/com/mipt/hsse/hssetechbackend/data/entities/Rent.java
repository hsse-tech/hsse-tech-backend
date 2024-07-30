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

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "user_id", nullable = false)
  private HumanUserPassport renter;

  public Rent(String name,
              String description,
              Instant plannedStart,
              Instant plannedEnd,
              HumanUserPassport renter,
              Item item) {
    this.name = name;
    this.description = description;
    this.plannedStart = plannedStart;
    this.plannedEnd = plannedEnd;
    this.item = item;
    this.renter = renter;
  }

  public Rent(
      String name,
      String description,
      Instant plannedStart,
      Instant plannedEnd,
      HumanUserPassport renter,
      Item item,
      Instant factEnd) {
    this(name, description, plannedStart, plannedEnd, renter, item);
    this.factEnd = factEnd;
  }

  public Rent(Instant plannedStart,
              Instant plannedEnd,
              HumanUserPassport renter,
              Item item) {
    this("Аренда", null, plannedStart, plannedEnd, renter, item);
  }
}
