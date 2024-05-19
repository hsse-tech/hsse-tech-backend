package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
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
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "original_id", nullable = false)
  private User user;

  @Column(name = "yandex_id", nullable = false)
  private String yandexId;

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

  public HumanUserPassport(String yandexId, String firstName, String lastName,
                           String email, User user) {
    this.yandexId = yandexId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.user = user;
  }

  public HumanUserPassport() {

  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return getUser().getRoles();
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return getYandexId();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return getIsBanned();
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
