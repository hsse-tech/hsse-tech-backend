package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaItemTypeRepository extends JpaRepository<ItemType, UUID> {}
