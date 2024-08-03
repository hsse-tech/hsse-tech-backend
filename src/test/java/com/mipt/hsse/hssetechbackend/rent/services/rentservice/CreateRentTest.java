package com.mipt.hsse.hssetechbackend.rent.services.rentservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepositoryOnDrive;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.UnoccupiedTimeCreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
@Import({
  RentService.class,
  PhotoRepositoryOnDrive.class,
  UnoccupiedTimeCreateRentProcessor.class
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CreateRentTest extends DatabaseSuite {
  private final HumanUserPassport user =
      new HumanUserPassport(123L, "Test", "User", "test@phystech.edu");
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "TestItemType", 60, false);
  private final Item item = new Item("TestItem", itemType);
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaHumanUserPassportRepository humanUserPassportRepository;
  @MockBean private JpaRentRepository rentRepository;
  @Autowired private RentService rentService;

  @BeforeEach
  void save() {
    humanUserPassportRepository.save(user);
    itemTypeRepository.save(itemType);
    itemRepository.save(item);
  }

  @AfterEach
  public void clear() {
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
  }

  @Test
  void testCreateRent() {
    when(rentRepository.isDisjointWithOtherRentsOfSameItem(any(), any(), any())).thenReturn(true);

    Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(item.getId(), startTime, endTime, "Test name", null);
    rentService.createRent(user.getId(), createRentRequest);

    verify(rentRepository).save(any());
  }

  @Test
  void testFailCreateWithNegativeDuration() {
    Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant endTime = Instant.now().plus(1, ChronoUnit.DAYS).minus(10, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(item.getId(), startTime, endTime, "Test name", null);

    assertThrows(
        VerificationFailedException.class, () -> rentService.createRent(user.getId(), createRentRequest));
  }

  @Test
  void testFailCreateWithTooLittleDuration() {
    Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(5, ChronoUnit.SECONDS);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(item.getId(), startTime, endTime, "Test name", null);

    assertThrows(
        VerificationFailedException.class, () -> rentService.createRent(user.getId(), createRentRequest));
  }

  @Test
  void testFailCreateWithTooLargeDuration() {
    when(rentRepository.isDisjointWithOtherRentsOfSameItem(any(), any(), any())).thenReturn(true);

    Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant endTime = Instant.now().plus(100, ChronoUnit.DAYS);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(item.getId(), startTime, endTime, "Test name", null);

    assertThrows(
        VerificationFailedException.class, () -> rentService.createRent(user.getId(), createRentRequest));
  }

  @Test
  void testCreateRentWithMaxEdgeDuration() {
    when(rentRepository.isDisjointWithOtherRentsOfSameItem(any(), any(), any())).thenReturn(true);

    Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(60, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(item.getId(), startTime, endTime, "Test name", null);
    rentService.createRent(user.getId(), createRentRequest);

    verify(rentRepository).save(any());
  }

  @Test
  void testFailCreateIntersectingRents() {
    when(rentRepository.isDisjointWithOtherRentsOfSameItem(any(), any(), any())).thenReturn(false);

    Instant startTime = Instant.now().plus(1, ChronoUnit.DAYS);
    Instant endTime = Instant.now().plus(1, ChronoUnit.DAYS).plus(60, ChronoUnit.MINUTES);
    CreateRentRequest createRentRequest =
        new CreateRentRequest(item.getId(), startTime, endTime, "Test name", null);

    assertThrows(RentProcessingException.class, () -> rentService.createRent(user.getId(), createRentRequest));
  }
}
