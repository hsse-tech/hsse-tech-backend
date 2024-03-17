package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaRentRepository extends JpaRepository<Rent, UUID> {}
