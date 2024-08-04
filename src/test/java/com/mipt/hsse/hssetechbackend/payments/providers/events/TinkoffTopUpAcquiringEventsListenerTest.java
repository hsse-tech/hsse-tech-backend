package com.mipt.hsse.hssetechbackend.payments.providers.events;

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.events.TinkoffTopUpAcquiringEventsListener;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.payments.services.WalletService;
import java.math.BigDecimal;
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
@Import({TinkoffTopUpAcquiringEventsListener.class, TransactionService.class, WalletService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class TinkoffTopUpAcquiringEventsListenerTest extends DatabaseSuite {
  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private TinkoffTopUpAcquiringEventsListener listener;

  @Autowired
  private JpaTransactionRepository transactionRepository;

  private Wallet testWallet;

  @BeforeEach
  public void setUp() {
    var testUserPassport = new HumanUserPassport(123L, "Test", "User", "test@phystech.edu");
    passportRepository.save(testUserPassport);
    testWallet = walletRepository.findByOwnerId(testUserPassport.getId());
    testWallet.setBalance(BigDecimal.valueOf(100));
    walletRepository.save(testWallet);
  }

  @AfterEach
  public void clear() {
    walletRepository.deleteAll();
    passportRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  @Test
  public void testAcceptingTopUp() {
    testNotification("CONFIRMED", -50, ClientTransactionStatus.SUCCESS, 150);
  }

  @Test
  public void testRejectingTopUpWhenTinkoffStatusIsReversed() {
    testNotification("REVERSED", -50, ClientTransactionStatus.FAILED, 100);
  }

  @Test
  public void testRejectingTopUpWhenTinkoffStatusRejected() {
    testNotification("REJECTED", -50, ClientTransactionStatus.FAILED, 100);
  }

  @Test
  public void testDoNothingWhenAnotherNotificationTypeReceived() {
    testNotification("SOME_ANOTHER_STATUS", -50, ClientTransactionStatus.IN_PROCESS, 100);
  }

  private void testNotification(String tinkoffStatus, int transAmount, ClientTransactionStatus expectedTransactionStatus, int expectedBalance) {
    var testTransaction = new Transaction(BigDecimal.valueOf(transAmount), "Test", null);
    testTransaction.setWallet(testWallet);
    transactionRepository.save(testTransaction);

    listener.onAcquiringNotificationReceived(
            new MerchantNotification(50 * 100, testTransaction.getId().toString(), true, tinkoffStatus));

    assertFalse(transactionRepository.findAll().isEmpty());
    assertFalse(walletRepository.findAll().isEmpty());

    var targetTransaction = transactionRepository.findAll().getFirst();
    var targetWallet = walletRepository.findAll().getFirst();

    assertEquals(testTransaction.getId().toString(), targetTransaction.getId().toString());
    assertEquals(expectedTransactionStatus, targetTransaction.getStatus());
    assertEquals(expectedBalance, targetWallet.getBalance());
  }
}
