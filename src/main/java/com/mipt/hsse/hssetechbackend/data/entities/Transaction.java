package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transaction")
public class Transaction {
  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "amount", nullable = false, precision = 9, scale = 2)
  private BigDecimal amount;

  @Column(name = "is_success", nullable = false)
  private Boolean isSuccess = false;

  @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
  private String name;

  @Column(name = "description", length = Integer.MAX_VALUE)
  private String description;

  @Column(name = "committed_at", nullable = false)
  private Instant committedAt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "wallet_id", nullable = false)
  private Wallet wallet;
}
