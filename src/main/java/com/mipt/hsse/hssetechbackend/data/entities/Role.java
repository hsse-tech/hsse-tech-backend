package com.mipt.hsse.hssetechbackend.data.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "role")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {
  @Id
  @Setter(AccessLevel.NONE)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
  private String name;

  @ManyToMany(mappedBy = "roles")
  private Set<HumanUserPassport> users = new LinkedHashSet<>();

  public Role(String name) {
    this.name = name;
  }

  public void addUser(HumanUserPassport user) {
    users.add(user);
    user.getRoles().add(this);
  }

  public void removeUser(HumanUserPassport user) {
    users.remove(user);
    user.getRoles().remove(this);
  }
}
