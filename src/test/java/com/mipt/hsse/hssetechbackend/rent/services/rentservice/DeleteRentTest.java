package com.mipt.hsse.hssetechbackend.rent.services.rentservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.UnoccupiedTimeCreateRentProcessor;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import java.math.BigDecimal;
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
class DeleteRentTest extends DatabaseSuite {
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaUserRepository userRepository;
  @Autowired private JpaHumanUserPassportRepository humanUserPassportRepository;

  @MockBean private JpaRentRepository rentRepository;

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
  public void clear() {
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
    userRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
  }

  @Test
  void testDeleteRent() {
    Rent rent = new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS), userPassport, item);
    when(rentRepository.findById(any())).thenReturn(Optional.of(rent));

    rentService.deleteRent(UUID.randomUUID());

    verify(rentRepository).delete(rent);
  }

  @Test
  void testFailDeleteNonExistingRent() {
    when(rentRepository.findById(any())).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> rentService.deleteRent(UUID.randomUUID()));
  }
}
