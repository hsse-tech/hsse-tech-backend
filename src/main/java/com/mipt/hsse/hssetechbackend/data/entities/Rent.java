package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "rent")
public class Rent {
  @Id
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


  protected Rent() {}

  public Rent(Instant from, Instant to, User renter, Item item) {
    this.from = from;
    this.to = to;
    this.item = item;
    this.renter = renter;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Instant getFrom() {
    return from;
  }

  public void setFrom(Instant from) {
    this.from = from;
  }

  public Instant getTo() {
    return to;
  }

  public void setTo(Instant to) {
    this.to = to;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Instant endedAt) {
    this.endedAt = endedAt;
  }

  public User getRenter() {
    return renter;
  }
}
