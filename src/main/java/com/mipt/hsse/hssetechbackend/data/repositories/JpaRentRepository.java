package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaRentRepository extends JpaRepository<Rent, UUID> {
  @Query("select count(*) from Rent where item = :item and not (endedAt <= :from or startAt >= :to)")
  int countRentsIntersectingTimeBounds(Item item, Instant from, Instant to);
}
