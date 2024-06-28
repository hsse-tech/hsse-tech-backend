package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.boot.model.naming.ImplicitTenantIdColumnNameSource;

@Getter
@Setter
@Entity
@Table(name = "lock_passport")
public class LockPassport {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "original_id", nullable = false)
  private UUID id;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "lock")
  private List<Item> lockedItems;

  @Column(name = "is_open", nullable = false)
  private boolean isOpen;

  public LockPassport() {
    lockedItems = new ArrayList<>();
    isOpen = false;
  }

  public void addItem(Item item) {
    lockedItems.add(item);
    item.setLock(this);
  }

  public void removeItem(Item item) {
    lockedItems.remove(item);
    item.setLock(null);
  }

  public boolean doesLockItem(Item item) {
    return item.getLock() != null && item.getLock().getId().equals(id);
  }
}
