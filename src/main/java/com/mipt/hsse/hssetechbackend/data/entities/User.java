package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Представляет сущность пользователя (замок или человек)
 */
@Entity
@Getter
@Setter
@Table(name = "\"user\"")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "user_type", nullable = false, length = Integer.MAX_VALUE)
  private String userType;

  public User(String userType) {
    this.userType = userType;
  }
}
