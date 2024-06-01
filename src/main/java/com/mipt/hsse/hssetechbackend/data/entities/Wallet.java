package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "wallet")
public class Wallet {
  @Id
  @ColumnDefault("gen_random_uuid()")
  @Column(name = "id", nullable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "owner_yandex_id", nullable = false, referencedColumnName = "yandex_id")
  private HumanUserPassport owner;

  @NotNull
  @ColumnDefault("0")
  @Column(name = "balance", nullable = false, precision = 9, scale = 2)
  private BigDecimal balance;

  @OneToMany(mappedBy = "wallet")
  private Set<Transaction> transactions = new LinkedHashSet<>();
}
