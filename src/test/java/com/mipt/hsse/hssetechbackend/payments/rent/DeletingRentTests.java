package com.mipt.hsse.hssetechbackend.payments.rent;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessData;
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

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({PaymentDeleteRentProcessor.class, TransactionService.class})
public class DeletingRentTests extends DatabaseSuite {
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
  private PaymentDeleteRentProcessor paymentRentProc;

  private HumanUserPassport testRenter;
  private Item testItem;

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
    Wallet testWallet = new Wallet();

    testWallet.setOwner(testRenter);
    testWallet.setBalance(BigDecimal.valueOf(150));

    humanUserPassportRepository.save(testRenter);
    walletRepository.save(testWallet);
    itemTypeRepository.save(testItemType);
    itemRepository.save(testItem);

    testRenter.setWallet(testWallet);
  }

  @Test
  public void testDeleteShouldPass() {
    var processData = new DeleteRentProcessData(
            new Rent(Instant.now(), Instant.now().plusSeconds(90 * 60), testRenter, testItem));

    var result = paymentRentProc.processDelete(processData);

    assertEquals(1, transactionRepository.count());

    var transaction = transactionRepository.findAll().get(0);

    assertTrue(result.isValid());
    assertEquals(300, walletRepository.findAll().get(0).getBalance());
    assertEquals("Возврат средств за аренду вещи \"Молоток с оранжевой рукоятью\"", transaction.getName());
    assertEquals(ClientTransactionStatus.SUCCESS, transaction.getStatus());
  }
}
