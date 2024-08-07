package com.mipt.hsse.hssetechbackend.payments.providers;

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionService;
import com.mipt.hsse.hssetechbackend.payments.services.WalletService;
import java.math.BigDecimal;
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
@Import({TopUpBalanceProvider.class, TransactionService.class, WalletService.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class TopUpBalanceTests extends DatabaseSuite {
  @MockBean
  private AcquiringSessionInitializer acquiringSessionInitializer;

  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private TopUpBalanceProviderBase topUpBalanceProviderBase;

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
    passportRepository.deleteAll();
    walletRepository.deleteAll();
    transactionRepository.deleteAll();
  }

  @Test
  public void testCreateTopUpRequest() {
    when(acquiringSessionInitializer.initialize(any()))
            .thenReturn(
                    new SessionData(100 * 100, "some guid", true, "TEST_URI"));

    TopUpSession result = topUpBalanceProviderBase.topUpBalance(testWallet.getId(), BigDecimal.valueOf(100));

    assertTrue(result.successfullyCreated());
    assertEquals("TEST_URI", result.paymentUrl());

    assertFalse(transactionRepository.findAll().isEmpty());
    assertFalse(walletRepository.findAll().isEmpty());

    var targetTransaction = transactionRepository.findAll().getFirst();
    var targetWallet = walletRepository.findAll().getFirst();

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

    TopUpSession result = topUpBalanceProviderBase.topUpBalance(testWallet.getId(), BigDecimal.valueOf(100));

    assertFalse(result.successfullyCreated());
    assertNull(result.paymentUrl());

    assertFalse(transactionRepository.findAll().isEmpty());
    assertFalse(walletRepository.findAll().isEmpty());

    var targetTransaction = transactionRepository.findAll().getFirst();
    var targetWallet = walletRepository.findAll().getFirst();

    assertEquals(-100, targetTransaction.getAmount());
    assertEquals(ClientTransactionStatus.FAILED, targetTransaction.getStatus());
    assertEquals(100, targetWallet.getBalance());

    verify(acquiringSessionInitializer).initialize(
            new SessionParams(100 * 100, targetTransaction.getId().toString()));
  }
}
