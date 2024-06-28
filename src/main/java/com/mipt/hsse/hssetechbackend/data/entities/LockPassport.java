package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lock_passport")
public class LockPassport {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "original_id", nullable = false)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id")
  private Item item;

  @Column(name = "is_open", nullable = false)
  private boolean isOpen;

  public LockPassport(Item item, boolean isOpen) {
    this.item = item;
    this.isOpen = isOpen;
  }

  public LockPassport(Item item) {
    this(item, false);
  }
}
