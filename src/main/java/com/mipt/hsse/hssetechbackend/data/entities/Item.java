package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "display_name", nullable = false, length = Integer.MAX_VALUE)
  private String displayName;

  @NotNull
  @ManyToOne(optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "type_id", nullable = false)
  private ItemType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "lock_id", nullable = true)
  private LockPassport lock;

  public Item(String displayName, ItemType type) {
    this.displayName = displayName;
    this.type = type;
  }
}
