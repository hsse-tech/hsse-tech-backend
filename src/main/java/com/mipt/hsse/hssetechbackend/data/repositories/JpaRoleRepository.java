package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.Role;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaRoleRepository extends JpaRepository<Role, UUID> {
    boolean existsByName(String name);

    Role getRoleByName(String name);
}
