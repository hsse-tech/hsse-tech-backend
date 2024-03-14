package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Entity
@Table(name = "wallet", schema = "hsse_tech")
public class Wallet {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "owner_yandex_id", nullable = false, referencedColumnName = "yandex_id")
  private HumanUserPassport ownerYandex;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public HumanUserPassport getOwnerYandex() {
    return ownerYandex;
  }

  public void setOwnerYandex(HumanUserPassport ownerYandex) {
    this.ownerYandex = ownerYandex;
  }
}
