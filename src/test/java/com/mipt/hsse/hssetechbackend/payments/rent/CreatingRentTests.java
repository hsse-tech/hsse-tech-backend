package com.mipt.hsse.hssetechbackend.payments.rent;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.createrentprocessing.CreateRentProcessData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({PaymentCreateRentProcessor.class, TransactionService.class})
public class CreatingRentTests extends DatabaseSuite {
  @Autowired
  private JpaUserRepository userRepository;

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
  private Wallet testWallet;

  @BeforeEach
  public void setUp() {
    userRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
    itemRepository.deleteAll();
    itemTypeRepository.deleteAll();
    walletRepository.deleteAll();
    transactionRepository.deleteAll();

    testRenter = new HumanUserPassport(123L, "Ivan", "Ivanov", "phystech@phystech.edu", new User("user"));
    var testItemType = new ItemType(BigDecimal.valueOf(100), "Молоток", null, false);
    testItem = new Item("Молоток с оранжевой рукоятью", testItemType);
    testWallet = new Wallet();

    testWallet.setOwner(testRenter);
    testWallet.setBalance(BigDecimal.valueOf(1000));

    humanUserPassportRepository.save(testRenter);
    walletRepository.save(testWallet);
    itemTypeRepository.save(testItemType);
    itemRepository.save(testItem);

    testRenter.setWallet(testWallet);
  }

  @Test
  public void testCreateShouldPass() {
    var processData = new CreateRentProcessData(new Rent(Instant.now(), Instant.now().plusSeconds(90 * 60), testRenter, testItem));

    paymentRentProc.processCreate(processData);

    assertEquals(1, transactionRepository.count());

    var transaction = transactionRepository.findAll().get(0);

    assertEquals(0, transaction.getAmount().compareTo(BigDecimal.valueOf(150.00)));
    assertEquals("Аренда \"Молоток с оранжевой рукоятью\"", transaction.getName());
    assertEquals(ClientTransactionStatus.SUCCESS, transaction.getStatus());
  }
}
