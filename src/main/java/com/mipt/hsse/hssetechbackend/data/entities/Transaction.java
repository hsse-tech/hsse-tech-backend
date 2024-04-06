package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "transaction")
@DynamicInsert
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @NotNull
  @Column(name = "amount", nullable = false, precision = 9, scale = 2)
  private BigDecimal amount;

  @NotNull
  @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ClientTransactionStatus status = ClientTransactionStatus.IN_PROCESS;

  public Transaction(BigDecimal amount, String name, String description) {
    this.amount = amount;
    this.name = name;
    this.description = description;
  }

  protected Transaction() {}
}
