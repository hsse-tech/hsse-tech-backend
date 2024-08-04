package com.mipt.hsse.hssetechbackend.payments.rent;

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.payments.services.WalletService;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessData;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@TestPropertySource("classpath:application-test.properties")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({PaymentCreateRentProcessor.class, TransactionService.class, WalletService.class})
public class PaymentCreatingRentTests extends DatabaseSuite {
  @Autowired
  private JpaHumanUserPassportRepository humanUserPassportRepository;

  @Autowired
  private JpaItemRepository itemRepository;

  @Autowired
  private JpaItemTypeRepository itemTypeRepository;

  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaTransactionRepository transactionRepository;

  @Autowired
  private PaymentCreateRentProcessor paymentRentProc;

  private HumanUserPassport testRenter;
  private Item testItem;

  @BeforeEach
  public void setUp() {
    testRenter = new HumanUserPassport(123L, "Ivan", "Ivanov", "phystech@phystech.edu");
    var testItemType = new ItemType(BigDecimal.valueOf(100), "Молоток", null, false);
    testItem = new Item("Молоток с оранжевой рукоятью", testItemType);

    humanUserPassportRepository.save(testRenter);
    itemTypeRepository.save(testItemType);
    itemRepository.save(testItem);

    var testWallet = walletRepository.findByOwnerId(testRenter.getId());
    testWallet.setBalance(BigDecimal.valueOf(150));
    testRenter.setWallet(testWallet);
    walletRepository.save(testWallet);
  }

  @AfterEach
  public void clear() {
    humanUserPassportRepository.deleteAll();
    itemRepository.deleteAll();
    itemTypeRepository.deleteAll();
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  @Test
  public void testCreateShouldPass() {
    var processData = new CreateRentProcessData(new Rent(Instant.now(), Instant.now().plusSeconds(90 * 60), testRenter, testItem));

    var result = paymentRentProc.processCreate(processData);

    assertEquals(1, transactionRepository.count());

    var transaction = transactionRepository.findAll().getFirst();

    assertTrue(result.isValid());
    assertEquals(BigDecimal.ZERO, walletRepository.findAll().getFirst().getBalance());
    assertEquals(0, transaction.getAmount().compareTo(BigDecimal.valueOf(150.00)));
    assertEquals("Оплата аренды", transaction.getName());
    assertEquals("Аренда \"Молоток с оранжевой рукоятью\"", transaction.getDescription());
    assertEquals(ClientTransactionStatus.SUCCESS, transaction.getStatus());
  }

  @Test
  public void testCreateShouldFailBecauseNotEnoughBalance() {
    var processData = new CreateRentProcessData(new Rent(Instant.now(), Instant.now().plusSeconds(120 * 60), testRenter, testItem));

    var result = paymentRentProc.processCreate(processData);

    assertEquals(0, transactionRepository.count());
    assertEquals(0, walletRepository.findAll().getFirst().getBalance().compareTo(BigDecimal.valueOf(150)));
    assertFalse(result.isValid());
  }

  @Test
  public void testCreateShouldFailBecauseWalletNotFound() {
    var processData = new CreateRentProcessData(new Rent(Instant.now(), Instant.now().plusSeconds(120 * 60), testRenter, testItem));
    var fakeWallet = new Wallet();
    fakeWallet.setId(UUID.randomUUID());

    testRenter.setWallet(fakeWallet);
    var result = paymentRentProc.processCreate(processData);

    assertEquals(0, transactionRepository.count());
    assertEquals(0, walletRepository.findAll().getFirst().getBalance().compareTo(BigDecimal.valueOf(150)));
    assertFalse(result.isValid());
  }
}
