package com.mipt.hsse.hssetechbackend.payments.rent;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.payments.services.WalletService;
import com.mipt.hsse.hssetechbackend.rent.rentprocessing.deleterentprocessing.DeleteRentProcessData;
import org.junit.jupiter.api.AfterEach;
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
@Import({PaymentDeleteRentProcessor.class, TransactionService.class, WalletService.class})
public class PaymentDeletingRentTests extends DatabaseSuite {
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
    testRenter = new HumanUserPassport(123L, "Ivan", "Ivanov", "phystech@phystech.edu");
    var testItemType = new ItemType(BigDecimal.valueOf(100), "Молоток", null, false);
    testItem = new Item("Молоток с оранжевой рукоятью", testItemType);


    humanUserPassportRepository.save(testRenter);
    itemTypeRepository.save(testItemType);
    itemRepository.save(testItem);

    humanUserPassportRepository.save(testRenter);
    var testWallet = walletRepository.findByOwnerId(testRenter.getId());
    testRenter.setWallet(testWallet);
    testWallet.setBalance(BigDecimal.valueOf(150));
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
  public void testDeleteShouldPass() {
    var processData = new DeleteRentProcessData(
            new Rent(Instant.now(), Instant.now().plusSeconds(90 * 60), testRenter, testItem));

    var result = paymentRentProc.processDelete(processData);

    assertEquals(1, transactionRepository.count());

    var transaction = transactionRepository.findAll().getFirst();

    assertTrue(result.isValid());
    assertEquals(300, walletRepository.findAll().getFirst().getBalance());
    assertEquals("Возврат средств за аренду вещи \"Молоток с оранжевой рукоятью\"", transaction.getName());
    assertEquals(ClientTransactionStatus.SUCCESS, transaction.getStatus());
  }
}
