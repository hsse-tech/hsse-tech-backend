package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaRentRepository extends JpaRepository<Rent, UUID> {
  @Query(
      "select (count(*) = 0) from Rent where item = :item and not (plannedEnd <= :from or plannedStart >= :to)")
  boolean isDisjointWithOtherRentsOfSameItem(Item item, Instant from, Instant to);

  @Query("select r from Rent r where r.item = :item and r.plannedStart > current_timestamp")
  List<Rent> findAllFutureRentsOfItem(Item item);
}
