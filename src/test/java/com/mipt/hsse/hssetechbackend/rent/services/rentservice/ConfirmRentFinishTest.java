package com.mipt.hsse.hssetechbackend.rent.services.rentservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepositoryOnDrive;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.UnoccupiedTimeCreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
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
@Import({RentService.class, PhotoRepositoryOnDrive.class, UnoccupiedTimeCreateRentProcessor.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ConfirmRentFinishTest extends DatabaseSuite {
  private final HumanUserPassport userPassport =
      new HumanUserPassport(123L, "Test", "User", "test@phystech.edu");
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "TestItemType", 60, true);
  private final Item item = new Item("TestItem", itemType);
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaHumanUserPassportRepository humanUserPassportRepository;
  @MockBean private JpaRentRepository rentRepository;
  @MockBean private PhotoRepository photoRepository;
  @Autowired private RentService rentService;

  @BeforeEach
  void save() {
    humanUserPassportRepository.save(userPassport);
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
  void testConfirmRent() throws IOException, NoSuchAlgorithmException {
    Rent rent =
        new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);
    rent.setFactStart(Instant.now());

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    UUID uuid = UUID.randomUUID();
    rentService.confirmRentFinish(uuid, photoBytes);

    verify(photoRepository)
        .save(eq(PhotoRepository.PhotoType.RENT_CONFIRMATION), eq(uuid), aryEq(photoBytes));
  }

  @Test
  void testFailConfirmNonExistingRent() {
    when(rentRepository.findById(any())).thenReturn(Optional.empty());

    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    assertThrows(
        EntityNotFoundException.class, () -> rentService.confirmRentFinish(uuid, photoBytes));
  }

  @Test
  void testFailConfirmNotStartedRent() {
    Rent rent =
        new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    assertThrows(
        VerificationFailedException.class, () -> rentService.confirmRentFinish(uuid, photoBytes));
  }

  @Test
  void testFailedConfirmAlreadyEndedRent() {
    Rent rent =
        new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);
    rent.setFactStart(Instant.now());
    rent.setFactEnd(Instant.now().plus(20, ChronoUnit.MINUTES));

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    assertThrows(
        VerificationFailedException.class, () -> rentService.confirmRentFinish(uuid, photoBytes));
  }

  @Test
  void testFailConfirmRentThatDoesNotRequireConfirm() {
    ItemType itemTypeWithoutConfirm = new ItemType(BigDecimal.ZERO, "someName", 60, false);
    Item itemWithoutConfirm = new Item("someItemName", itemTypeWithoutConfirm);
    itemTypeRepository.save(itemTypeWithoutConfirm);
    itemRepository.save(itemWithoutConfirm);

    Rent rent =
        new Rent(
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.HOURS),
            userPassport,
            itemWithoutConfirm);
    rent.setFactStart(Instant.now());

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    assertThrows(
        VerificationFailedException.class, () -> rentService.confirmRentFinish(uuid, photoBytes));
  }

  @Test
  void testFailConfirmSecondTime() {
    Rent rent =
        new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);
    rent.setFactStart(Instant.now());

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));
    when(photoRepository.existsPhoto(eq(PhotoRepository.PhotoType.RENT_CONFIRMATION), any()))
        .thenReturn(true);

    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    assertThrows(
        VerificationFailedException.class,
        () -> rentService.confirmRentFinish(UUID.randomUUID(), photoBytes));
  }
}
