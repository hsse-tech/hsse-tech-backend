package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "wallet")
public class Wallet {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "owner_yandex_id", nullable = false, referencedColumnName = "yandex_id")
  private HumanUserPassport ownerYandex;

  @Column(name = "balance", nullable = false, precision = 9, scale = 2)
  private BigDecimal balance = BigDecimal.ZERO;

  @OneToMany(mappedBy = "wallet")
  private Set<Transaction> transactions = new LinkedHashSet<>();

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public HumanUserPassport getOwner() {
    return ownerYandex;
  }

  public void setOwner(HumanUserPassport ownerYandex) {
    this.ownerYandex = ownerYandex;
  }

  public BigDecimal getBalance() {
    return balance;
  }

  public void setBalance(BigDecimal balance) {
    this.balance = balance;
  }

  public Set<Transaction> getTransactions() {
    return transactions;
  }

  public void setTransactions(Set<Transaction> transactions) {
    this.transactions = transactions;
  }

}