package com.mipt.hsse.hssetechbackend.rent.services.rentservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;
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
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "TestItemType", 60, false);
  private final Item item = new Item("TestItem", itemType);

  @Autowired private RentService rentService;

  @BeforeEach
  void save() {
    humanUserPassportRepository.save(userPassport);
    userRepository.save(user);
    itemTypeRepository.save(itemType);
    itemRepository.save(item);
  }

  @AfterEach
  void clear() {
    rentRepository.deleteAll();
    itemRepository.deleteAll();
    itemTypeRepository.deleteAll();
    userRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
  }

  @Test
  void textUpdateRent() {
    Instant plannedStart = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant plannedEnd = Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);

    // Create rent
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), plannedStart, plannedEnd);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Update rent
    Instant newPlannedStart = Instant.now().plus(2, ChronoUnit.DAYS);
    Instant newPlannedEnd = Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);

    UpdateRentRequest updateRequest = new UpdateRentRequest(newPlannedStart, newPlannedEnd);
    rentService.updateRent(rentId, updateRequest);

    // Assert rent
    Rent receivedRent = rentService.findById(rentId);
    assertNotNull(receivedRent);
    assertEquals(rentId, receivedRent.getId());
    assertEquals(newPlannedStart, receivedRent.getPlannedStart());
    assertEquals(newPlannedEnd, receivedRent.getPlannedEnd());
  }

  @Test
  void testFailUpdateOccupiedTime() {
    // Create our rent
    Instant plannedStart = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant plannedEnd = Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), plannedStart, plannedEnd);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Create rent that will intersect ours
    CreateRentRequest createSecondRentRequest =
        new CreateRentRequest(
            user.getId(),
            item.getId(),
            Instant.now().plus(2, ChronoUnit.DAYS),
            Instant.now().plus(2, ChronoUnit.DAYS).plus(50, ChronoUnit.MINUTES));
    rentService.createRent(createSecondRentRequest);

    // Update the first rent so that it intersects the second rent
    Instant newPlannedStart = Instant.now().plus(2, ChronoUnit.DAYS);
    Instant newPlannedEnd = Instant.now().plus(2, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES);

    UpdateRentRequest updateRequest = new UpdateRentRequest(newPlannedStart, newPlannedEnd);
    assertThrows(RentProcessingException.class, () -> rentService.updateRent(rentId, updateRequest));
  }

  @Test
  void testFailUpdateRentWithInvalidTimeBounds() {
    Instant plannedStart = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant plannedEnd = Instant.now().plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);

    // Create rent
    CreateRentRequest createRentRequest =
        new CreateRentRequest(user.getId(), item.getId(), plannedStart, plannedEnd);
    UUID rentId = rentService.createRent(createRentRequest).getId();

    // Update rent
    Instant newPlannedStart = Instant.now().plus(2, ChronoUnit.DAYS);
    Instant newPlannedEnd = Instant.now().plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.SECONDS);

    UpdateRentRequest updateRequest = new UpdateRentRequest(newPlannedStart, newPlannedEnd);
    assertThrows(VerificationFailedException.class, () -> rentService.updateRent(rentId, updateRequest));
  }
}
