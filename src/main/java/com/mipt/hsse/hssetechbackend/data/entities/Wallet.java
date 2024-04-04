package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "wallet")
@Getter
@Setter
public class Wallet {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "owner_yandex_id", nullable = false, referencedColumnName = "yandex_id")
  private HumanUserPassport owner;

  @Column(name = "balance", nullable = false, precision = 9, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @OneToMany(mappedBy = "wallet", orphanRemoval = true, cascade = CascadeType.PERSIST)
  private List<Transaction> transactions = new ArrayList<>();
}