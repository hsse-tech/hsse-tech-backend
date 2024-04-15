package com.mipt.hsse.hssetechbackend.rent.services.rentservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.PinPhotoConfirmationRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
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
class ConfirmRentFinishTest extends DatabaseSuite {
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaUserRepository userRepository;
  @Autowired private JpaHumanUserPassportRepository humanUserPassportRepository;

  @MockBean private JpaRentRepository rentRepository;
  @MockBean private ConfirmationPhotoRepository photoRepository;

  private final User user = new User("test");
  private final HumanUserPassport userPassport =
      new HumanUserPassport(123L, "Test", "User", "test@phystech.edu", user);
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "TestItemType", 60, true);
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
  public void clear() {
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
    userRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
  }

  @Test
  void testConfirmRent() throws IOException, NoSuchAlgorithmException {
    Rent rent = new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);
    rent.setFactStart(Instant.now());

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    byte[] imageBytes = new byte[] {1, 2, 3};
    UUID uuid = UUID.randomUUID();
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(imageBytes);
    rentService.confirmRentFinish(uuid, request);

    verify(photoRepository).save(uuid, imageBytes);
  }

  @Test
  void testFailConfirmNonExistingRent() {
    when(rentRepository.findById(any())).thenReturn(Optional.empty());

    byte[] imageBytes = new byte[] {1, 2, 3};
    UUID uuid = UUID.randomUUID();
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(imageBytes);
    assertThrows(EntityNotFoundException.class, () -> rentService.confirmRentFinish(uuid, request));
  }

  @Test
  void testFailConfirmNotStartedRent() {
    Rent rent = new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    byte[] imageBytes = new byte[] {1, 2, 3};
    UUID uuid = UUID.randomUUID();
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(imageBytes);
    assertThrows(VerificationFailedException.class, () -> rentService.confirmRentFinish(uuid, request));
  }

  @Test
  void testFailedConfirmAlreadyEndedRent() {
    Rent rent = new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);
    rent.setFactStart(Instant.now());
    rent.setFactEnd(Instant.now().plus(20, ChronoUnit.MINUTES));

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    byte[] imageBytes = new byte[] {1, 2, 3};
    UUID uuid = UUID.randomUUID();
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(imageBytes);
    assertThrows(VerificationFailedException.class, () -> rentService.confirmRentFinish(uuid, request));
  }

  @Test
  void testFailConfirmRentThatDoesNotRequireConfirm() {
    ItemType itemTypeWithoutConfirm = new ItemType(BigDecimal.ZERO, "someName", 60, false);
    Item itemWithoutConfirm = new Item("someItemName", itemTypeWithoutConfirm);
    itemTypeRepository.save(itemTypeWithoutConfirm);
    itemRepository.save(itemWithoutConfirm);

    Rent rent = new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, itemWithoutConfirm);
    rent.setFactStart(Instant.now());

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    byte[] imageBytes = new byte[] {1, 2, 3};
    UUID uuid = UUID.randomUUID();
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(imageBytes);
    assertThrows(VerificationFailedException.class, () -> rentService.confirmRentFinish(uuid, request));
  }

  @Test
  void testFailConfirmSecondTime() {
    Rent rent = new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.HOURS), userPassport, item);
    rent.setFactStart(Instant.now());

    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));
    when(photoRepository.existsPhotoForRent(any())).thenReturn(true);

    byte[] imageBytes = new byte[] {1, 2, 3};
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(imageBytes);

    assertThrows(VerificationFailedException.class, () -> rentService.confirmRentFinish(UUID.randomUUID(), request));
  }
}
