package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaLockPassportRepository extends JpaRepository<LockPassport, UUID> {
}
