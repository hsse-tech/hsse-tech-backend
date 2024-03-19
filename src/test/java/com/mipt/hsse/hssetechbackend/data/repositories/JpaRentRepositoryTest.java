package com.mipt.hsse.hssetechbackend.data.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaRentRepositoryTest {
  @Autowired private JpaRentRepository rentRepository;
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaUserRepository userRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;

  Instant firstFrom = Instant.ofEpochSecond(0);
  Instant firstTo = Instant.ofEpochSecond(100);
  Instant secondFrom = Instant.ofEpochSecond(100);
  Instant secondTo = Instant.ofEpochSecond(200);
  Instant thirdFrom = Instant.ofEpochSecond(200);
  Instant thirdTo = Instant.ofEpochSecond(300);

  private final User user = new User("user");
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "name", 200, false);
  private final Item item1 = new Item("item1", itemType);

  private final Rent firstRentForItem1 = new Rent(firstFrom, firstTo, user, item1);
  private final Rent secondRentForItem1 = new Rent(secondFrom, secondTo, user, item1);
  private final Rent thirdRentForItem1 = new Rent(thirdFrom, thirdTo, user, item1);

  @BeforeEach
  void save() {
    itemTypeRepository.save(itemType);
    itemRepository.save(item1);
    userRepository.save(user);
  }

  @Test
  void testCountTimeBoundsIntersectionInBetween() {
    rentRepository.save(firstRentForItem1);
    rentRepository.save(thirdRentForItem1);

    assertEquals(
        rentRepository.countRentsIntersectingTimeBounds(
            secondRentForItem1.getItem(),
            secondRentForItem1.getStartAt(),
            secondRentForItem1.getEndedAt()),
        0);
  }
}
