package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;

import java.util.UUID;

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

  public HumanUserPassport(Long yandexId, String firstName, String lastName, String email) {
    this.yandexId = yandexId;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  public HumanUserPassport() {

  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Long getYandexId() {
    return yandexId;
  }

  public void setYandexId(Long yandexId) {
    this.yandexId = yandexId;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Boolean getIsBanned() {
    return isBanned;
  }

  public void setIsBanned(Boolean isBanned) {
    this.isBanned = isBanned;
  }
}
