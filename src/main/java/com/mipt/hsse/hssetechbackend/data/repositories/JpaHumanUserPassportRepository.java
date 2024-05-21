package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaHumanUserPassportRepository extends JpaRepository<HumanUserPassport, UUID> {
    Optional<HumanUserPassport> findHumanUserPassportByYandexId(Long id);
    HumanUserPassport findHumanUserPassportById(UUID id);
    boolean existsByYandexId(Long id);
}
