package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "human_user_passport")
public class HumanUserPassport {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "original_id", nullable = false)
  @Setter(AccessLevel.NONE)
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "original_id", nullable = false)
  private User user;

  @Column(name = "yandex_id", nullable = false)
  private Long yandexId;

  @Column(name = "first_name", nullable = false, length = Integer.MAX_VALUE)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = Integer.MAX_VALUE)
  private String lastName;

  @Column(name = "is_banned", nullable = false)
  private Boolean isBanned = false;

  @Column(name = "email", columnDefinition = "email")
  private String email;
}
