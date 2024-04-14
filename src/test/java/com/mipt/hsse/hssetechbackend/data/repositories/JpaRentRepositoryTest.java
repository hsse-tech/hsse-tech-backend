package com.mipt.hsse.hssetechbackend.data.repositories;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;

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
class JpaRentRepositoryTest extends DatabaseSuite {
  @Autowired private JpaRentRepository rentRepository;
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaUserRepository userRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaHumanUserPassportRepository humanRepository;

  // region Test objects

  // Timeline for tests:
  // <--firstRentItem1--><--secondRentItem1--><--thirdRentItem1-->
  // <---largeRentForItem1---->
  // <------rentForItem2------>

  Instant firstFrom = Instant.ofEpochSecond(0);
  Instant firstTo = Instant.ofEpochSecond(100);
  Instant secondFrom = Instant.ofEpochSecond(100);
  Instant secondTo = Instant.ofEpochSecond(200);
  Instant thirdFrom = Instant.ofEpochSecond(200);
  Instant thirdTo = Instant.ofEpochSecond(300);
  Instant largeRentFrom = Instant.ofEpochSecond(0);
  Instant largeRentTo = Instant.ofEpochSecond(150);

  private final User user = new User("user");
  private final HumanUserPassport humanUser = new HumanUserPassport(123L, "Test", "User", "test@phystech.edu", user);
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "name", 200, false);
  private final Item item1 = new Item("item1", itemType);
  private final Item item2 = new Item("item2", itemType);

  private final Rent firstRentForItem1 = new Rent(firstFrom, firstTo, humanUser, item1);
  private final Rent secondRentForItem1 = new Rent(secondFrom, secondTo, humanUser, item1);
  private final Rent thirdRentForItem1 = new Rent(thirdFrom, thirdTo, humanUser, item1);
  private final Rent largeRentForItem1 = new Rent(largeRentFrom, largeRentTo, humanUser, item1);
  private final Rent largeRentForItem2 = new Rent(largeRentFrom, largeRentTo, humanUser, item2);

  // endregion

  @BeforeEach
  void save() {
    rentRepository.deleteAll();
    userRepository.deleteAll();
    itemRepository.deleteAll();
    itemTypeRepository.deleteAll();
    humanRepository.deleteAll();

    itemTypeRepository.save(itemType);
    itemRepository.save(item1);
    itemRepository.save(item2);
    humanRepository.save(humanUser);
    userRepository.save(user);
  }

  @Test
  void canPutRentInBetweenTwo() {
    rentRepository.save(firstRentForItem1);
    rentRepository.save(thirdRentForItem1);

    assertTrue(
        rentRepository.isDisjointWithOtherRentsOfSameItem(
            secondRentForItem1.getItem(),
            secondRentForItem1.getPlannedStart(),
            secondRentForItem1.getPlannedEnd()));
    System.out.println(rentRepository.count());
  }

  @Test
  void failedPutRentInBetweenTwo() {
    rentRepository.save(largeRentForItem1);
    rentRepository.save(thirdRentForItem1);

    assertFalse(
        rentRepository.isDisjointWithOtherRentsOfSameItem(
            secondRentForItem1.getItem(),
            secondRentForItem1.getPlannedStart(),
            secondRentForItem1.getPlannedEnd()));
    System.out.println(rentRepository.count());
  }

  @Test
  void checkRentsForOtherItemsDoNotInterfere() {
    rentRepository.save(firstRentForItem1);
    rentRepository.save(thirdRentForItem1);
    rentRepository.save(
        largeRentForItem2); // this rent intersects with secondRent, but it is for another item

    assertTrue(
        rentRepository.isDisjointWithOtherRentsOfSameItem(
            secondRentForItem1.getItem(),
            secondRentForItem1.getPlannedStart(),
            secondRentForItem1.getPlannedEnd()));
  }

  @Test
  void failedRentOnTopOfAnother() {
    rentRepository.save(largeRentForItem1);

    assertFalse(
        rentRepository.isDisjointWithOtherRentsOfSameItem(
            largeRentForItem1.getItem(),
            largeRentForItem1.getPlannedStart(),
            largeRentForItem1.getPlannedEnd()));
  }
}
