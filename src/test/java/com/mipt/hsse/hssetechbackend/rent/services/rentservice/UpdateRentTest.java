package com.mipt.hsse.hssetechbackend.rent.services.rentservice;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.UnoccupiedTimeCreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import java.math.BigDecimal;
import java.time.Duration;
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
  ConfirmationPhotoRepositoryOnDrive.class,
  UnoccupiedTimeCreateRentProcessor.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UpdateRentTest extends DatabaseSuite {
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaUserRepository userRepository;
  @Autowired private JpaHumanUserPassportRepository humanUserPassportRepository;

  @Autowired private JpaRentRepository rentRepository;

  private final User user = new User("test");
  private final HumanUserPassport userPassport =
      new HumanUserPassport(123L, "Test", "User", "test@phystech.edu", user);
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "TestItemType", 120, false);
  private final Item item = new Item("TestItem", itemType);

  @Autowired private RentService rentService;

  @BeforeEach
  void save() {
    rentRepository.deleteAll();
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
    userRepository.deleteAll();
    humanUserPassportRepository.deleteAll();

    humanUserPassportRepository.save(userPassport);
    userRepository.save(user);
    itemTypeRepository.save(itemType);
    itemRepository.save(item);
  }

  @AfterEach
  void clear() {
    rentRepository.deleteAll();
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
    userRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
  }

  @Test
  void testUpdateRentTimeBounds() {
    // Create rent (it starts in 1 minute after now)
    final Instant startTime = Instant.now().plus(1, ChronoUnit.MINUTES);
    final Instant endTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Update rent
    final Instant newStartTime = Instant.now().plus(10, ChronoUnit.MINUTES);
    final Instant newEndTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    rentService.updateRent(rentId, new UpdateRentRequest(newStartTime, newEndTime));

    Rent retrievedRent = rentService.findById(rentId);
    assertTrue(equalsInstantsWithDelta(retrievedRent.getPlannedStart(), newStartTime));
    assertTrue(equalsInstantsWithDelta(retrievedRent.getPlannedEnd(), newEndTime));
  }

  @Test
  void testFailUpdateNonExistingRent() {
    final Instant newStartTime = Instant.now().plus(10, ChronoUnit.MINUTES);
    final Instant newEndTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    UpdateRentRequest request = new UpdateRentRequest(newStartTime, newEndTime);
    assertThrows(
        EntityNotFoundException.class, () -> rentService.updateRent(UUID.randomUUID(), request));
  }

  @Test
  void testFailUpdateRentAfterPlannedStartTime() {
    // Create rent
    Instant startTime = Instant.now().minus(1, ChronoUnit.MINUTES);
    Instant endTime = Instant.now().plus(50, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Fail update rent
    UpdateRentRequest updateRentRequest =
        new UpdateRentRequest(startTime.plus(1, ChronoUnit.DAYS), endTime.plus(1, ChronoUnit.DAYS));
    assertThrows(
        VerificationFailedException.class, () -> rentService.updateRent(rentId, updateRentRequest));
  }

  @Test
  void testFailUpdateRentWithNewPlannedEndAfterNow() {
    // Create rent
    Instant startTime = Instant.now().minus(3, ChronoUnit.HOURS);
    Instant endTime = Instant.now().minus(2, ChronoUnit.HOURS);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Fail update to passed time
    UpdateRentRequest updateRentRequest =
        new UpdateRentRequest(
            Instant.now().minus(1, ChronoUnit.DAYS),
            Instant.now().minus(1, ChronoUnit.DAYS).plus(40, ChronoUnit.MINUTES));
    assertThrows(
        VerificationFailedException.class, () -> rentService.updateRent(rentId, updateRentRequest));
  }

  @Test
  void testFailUpdateIfIntersectsWithAnother() {
    // Create test rent
    Instant startTime = Instant.now().plus(1, ChronoUnit.HOURS);
    Instant endTime = Instant.now().plus(2, ChronoUnit.HOURS);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Create rent that will intersect with the test one after update
    startTime = Instant.now().plus(1, ChronoUnit.DAYS);
    endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
    CreateRentRequest createRentIntersectingRequest =
        new CreateRentRequest(user.getId(), item.getId(), startTime, endTime);
    rentService.createRent(createRentIntersectingRequest);

    // Fail update test rent onto another one
    UpdateRentRequest updateRentRequest =
        new UpdateRentRequest(
            Instant.now().plus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES),
            Instant.now().plus(3, ChronoUnit.DAYS));
    assertThrows(
        VerificationFailedException.class, () -> rentService.updateRent(rentId, updateRentRequest));
  }

  // Compare Instants with delta due to Instant.now() inaccuracies
  // Two instants are considered equal if the difference is less than 1 millisecond
  private boolean equalsInstantsWithDelta(Instant a, Instant b) {
    return Duration.between(a, b).toMillis() == 0;
  }
}
