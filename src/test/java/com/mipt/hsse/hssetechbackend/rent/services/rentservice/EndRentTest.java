package com.mipt.hsse.hssetechbackend.rent.services.rentservice;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepositoryOnDrive;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.UnoccupiedTimeCreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({
  RentService.class,
  PhotoRepositoryOnDrive.class,
  UnoccupiedTimeCreateRentProcessor.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EndRentTest extends DatabaseSuite {
  private final HumanUserPassport user =
      new HumanUserPassport(123L, "Test", "User", "test@phystech.edu");
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "TestItemType", 60, false);
  private final Item item = new Item("TestItem", itemType);
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaHumanUserPassportRepository humanUserPassportRepository;
  @Autowired private JpaRentRepository rentRepository;
  @Autowired private RentService rentService;

  @BeforeEach
  void save() {
    humanUserPassportRepository.save(user);
    itemTypeRepository.save(itemType);
    itemRepository.save(item);
  }

  @AfterEach
  public void clear() {
    rentRepository.deleteAll();
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
  }

  @Test
  void testEndRent() {
    // Create rent
    Instant startTime = Instant.now().minus(1, ChronoUnit.MINUTES);
    Instant endTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Start rent
    rentService.startRent(rentId);

    Instant before = Instant.now();
    rentService.endRent(rentId);
    Instant after = Instant.now();

    Rent retrievedRent = rentService.findById(rentId);
    assertNotNull(retrievedRent.getFactEnd());
    assertTrue(retrievedRent.getFactEnd().isAfter(before));
    assertTrue(retrievedRent.getFactEnd().isBefore(after));
  }

  @Test
  void testFailEndNonExistingRent() {
    assertThrows(EntityNotFoundException.class, () -> rentService.endRent(UUID.randomUUID()));
  }

  @Test
  void testFailEndAlreadyEndedRent() {
    // Create rent
    Instant startTime = Instant.now().minus(1, ChronoUnit.MINUTES);
    Instant endTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Start rent
    rentService.startRent(rentId);
    rentService.endRent(rentId);

    // Start again
    assertThrows(VerificationFailedException.class, () -> rentService.endRent(rentId));
  }

  @Test
  void testFailEndNotStartedRent() {
    // Create rent
    Instant startTime = Instant.now().minus(5, ChronoUnit.MINUTES);
    Instant endTime = Instant.now().plus(30, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Start rent
    assertThrows(VerificationFailedException.class, () -> rentService.endRent(rentId));
  }

  @Test
  void testFailEndRentWithoutRequiredPhoto() {
    ItemType itemTypeWithPhoto = new ItemType(BigDecimal.ZERO, "someName", 60, true);
    Item itemWithPhoto = new Item("testItem", itemTypeWithPhoto);
    itemTypeRepository.save(itemTypeWithPhoto);
    itemRepository.save(itemWithPhoto);

    // Create rent
    Instant startTime = Instant.now().minus(3, ChronoUnit.MINUTES);
    Instant endTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), itemWithPhoto.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    rentService.startRent(rentId);

    assertThrows(VerificationFailedException.class, () -> rentService.endRent(rentId));
  }
}
