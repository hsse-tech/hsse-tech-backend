package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JpaHumanUserPassportRepository extends JpaRepository<HumanUserPassport, UUID> {
    Optional<HumanUserPassport> findHumanUserPassportByYandexId(String id);
    HumanUserPassport findHumanUserPassportById(UUID id);
    boolean existsByYandexId(String id);
}
