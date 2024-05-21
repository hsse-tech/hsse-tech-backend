package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaItemTypeRepository extends JpaRepository<ItemType, UUID> {}
