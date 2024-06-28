package com.mipt.hsse.hssetechbackend.data.repositories;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaRentRepository extends JpaRepository<Rent, UUID> {
  @Query(
      "select (count(*) = 0) from Rent where item = :item and not (plannedEnd <= :from or plannedStart >= :to)")
  boolean isDisjointWithOtherRentsOfSameItem(Item item, Instant from, Instant to);

  @Query(
      "select r from Rent r where r.item = :item and not (r.plannedEnd <= :from or r.plannedStart >= :to)")
  List<Rent> getIntersectingRentsOfItem(Item item, Instant from, Instant to);

  // Функция "current_timestamp" в JPQL возвращает время на сервере, т.е. с учетом часового пояса, а мы работает в UTC
  // Сам JPQL не имеет понятия о часовых поясах и работе с ними
  // Поэтому здесь приходится использовать nativeQuery в писать на PostgreSQL
  // Мне не нравится эта привязка к субд, но как сделать нормально я не знаю
  // TODO: Есть идея хранить время в MSK; возможно, потом этим займусь

  @Query(value = "SELECT * FROM Rent r WHERE r.item_id = :itemId AND r.from > (NOW() AT TIME ZONE 'UTC')", nativeQuery = true)
  List<Rent> findAllFutureRentsOfItem(@Param("itemId") UUID itemId);

  @Query(value = "SELECT * FROM Rent r WHERE r.item_id = :itemId AND r.from < (NOW() AT TIME ZONE 'UTC') AND (NOW() AT TIME ZONE 'UTC') < r.to", nativeQuery = true)
  Rent getCurrentRentOfItem(@Param("itemId") UUID itemId);
}
