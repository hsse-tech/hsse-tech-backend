package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Представляет сущность человек, зашедшего под Yandex ID
 */
@Setter
@Getter
@Entity
@Table(name = "human_user_passport")
public class HumanUserPassport {
  @Id
  @Column(name = "original_id", nullable = false)
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

  @OneToMany(mappedBy = "renter")
  private List<Rent> rents;

  public HumanUserPassport(Long yandexId, String firstName, String lastName, String email, User user) {
    this.yandexId = yandexId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.user = user;
  }

  public HumanUserPassport() {

  }
}
