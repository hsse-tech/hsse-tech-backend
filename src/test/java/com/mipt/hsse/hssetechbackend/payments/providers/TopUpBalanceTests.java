package com.mipt.hsse.hssetechbackend.payments.providers;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.payments.services.WalletService;
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

import java.math.BigDecimal;

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import({TopUpBalanceProvider.class, TransactionService.class, WalletService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TopUpBalanceTests extends DatabaseSuite {
  @MockBean
  private AcquiringSessionInitializer acquiringSessionInitializer;

  @Autowired
  private JpaUserRepository userRepository;

  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private TopUpBalanceProvider topUpBalanceProvider;

  @Autowired
  private JpaTransactionRepository transactionRepository;

  private Wallet testWallet;

  @BeforeEach
  public void setUp() {
    var testUser = new User("test");
    var testUserPassport = new HumanUserPassport(123L, "Test", "User", "test@phystech.edu", testUser);
    testWallet = new Wallet();
    testWallet.setBalance(BigDecimal.valueOf(100.00));

    testWallet.setOwner(testUserPassport);
    testUserPassport.setUser(testUser);

    passportRepository.save(testUserPassport);
    userRepository.save(testUser);
    walletRepository.save(testWallet);
  }

  @AfterEach
  public void clear() {
    userRepository.deleteAll();
    passportRepository.deleteAll();
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  @Test
  public void testCreateTopUpRequest() {
    when(acquiringSessionInitializer.initialize(any()))
            .thenReturn(
                    new SessionData(100 * 100, "some guid", true, "TEST_URI"));

    TopUpSession result = topUpBalanceProvider.topUpBalance(testWallet.getId(), BigDecimal.valueOf(100));

    assertTrue(result.successfullyCreated());
    assertEquals("TEST_URI", result.paymentUrl());

    assertFalse(transactionRepository.findAll().isEmpty());
    assertFalse(walletRepository.findAll().isEmpty());

    var targetTransaction = transactionRepository.findAll().get(0);
    var targetWallet = walletRepository.findAll().get(0);

    assertEquals(-100, targetTransaction.getAmount());
    assertEquals(ClientTransactionStatus.IN_PROCESS, targetTransaction.getStatus());
    assertEquals(100, targetWallet.getBalance());

    verify(acquiringSessionInitializer).initialize(
            new SessionParams(100 * 100, targetTransaction.getId().toString()));
  }

  @Test
  public void testCreateTopUpRequestButInitializationFailed() {
    when(acquiringSessionInitializer.initialize(any()))
            .thenReturn(new SessionData(0, null, false, null));

    TopUpSession result = topUpBalanceProvider.topUpBalance(testWallet.getId(), BigDecimal.valueOf(100));

    assertFalse(result.successfullyCreated());
    assertNull(result.paymentUrl());

    assertFalse(transactionRepository.findAll().isEmpty());
    assertFalse(walletRepository.findAll().isEmpty());

    var targetTransaction = transactionRepository.findAll().get(0);
    var targetWallet = walletRepository.findAll().get(0);

    assertEquals(-100, targetTransaction.getAmount());
    assertEquals(ClientTransactionStatus.FAILED, targetTransaction.getStatus());
    assertEquals(100, targetWallet.getBalance());

    verify(acquiringSessionInitializer).initialize(
            new SessionParams(100 * 100, targetTransaction.getId().toString()));
  }
}
