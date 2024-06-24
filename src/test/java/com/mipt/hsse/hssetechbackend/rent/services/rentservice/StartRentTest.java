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
class StartRentTest extends DatabaseSuite {
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
  void testStartRent() {
    // Create rent
    Instant startTime = Instant.now().minus(1, ChronoUnit.MINUTES);
    Instant endTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Start rent
    Instant before = Instant.now();
    rentService.startRent(rentId);
    Instant after = Instant.now();

    Rent retrievedRent = rentService.findById(rentId);
    assertNotNull(retrievedRent.getFactStart());
    assertTrue(retrievedRent.getFactStart().isAfter(before));
    assertTrue(retrievedRent.getFactStart().isBefore(after));
  }

  @Test
  void testFailStartNonExistingRent() {
    assertThrows(EntityNotFoundException.class, () -> rentService.startRent(UUID.randomUUID()));
  }

  @Test
  void testFailStartAlreadyStartedRent() {
    // Create rent
    Instant startTime = Instant.now().minus(1, ChronoUnit.MINUTES);
    Instant endTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Start rent
    rentService.startRent(rentId);

    // Start again
    assertThrows(VerificationFailedException.class, () -> rentService.startRent(rentId));
  }

  @Test
  void testFailStartBeforePlannedStartTime() {
    // Create rent
    Instant startTime = Instant.now().plus(1, ChronoUnit.HOURS);
    Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Start rent
    assertThrows(VerificationFailedException.class, () -> rentService.startRent(rentId));
  }

  @Test
  void testFailedStartAfterPlannedEndTime() {
    // Create rent
    Instant startTime = Instant.now().minus(3, ChronoUnit.HOURS);
    Instant endTime = Instant.now().minus(2, ChronoUnit.HOURS);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Start rent
    assertThrows(VerificationFailedException.class, () -> rentService.startRent(rentId));
  }
}
