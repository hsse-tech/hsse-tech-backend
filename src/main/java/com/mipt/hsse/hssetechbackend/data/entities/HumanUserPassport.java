package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Представляет сущность человек, зашедшего под Yandex ID
 */
@Setter
@Getter
@Entity
@Table(name = "human_user_passport")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HumanUserPassport {
  @Id
  @Column(name = "original_id", nullable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

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

  @OneToOne(mappedBy = "owner")
  private Wallet wallet;

  @ManyToMany
  @JoinTable(name = "passport_role",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new LinkedHashSet<>();

  public HumanUserPassport(Long yandexId, String firstName, String lastName, String email) {
    this.yandexId = yandexId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }
}
