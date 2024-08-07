package com.mipt.hsse.hssetechbackend.payments.services;

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.*;
import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import java.math.BigDecimal;
import java.util.Optional;
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
@Import({TransactionService.class, WalletService.class})
public class TransactionServiceCreatingTransactionTest extends DatabaseSuite {
  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private TransactionService transactionService;

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
  }

  @Test
  public void testTransactionCreation() {
    var transactionInfo = new TransactionInfo(BigDecimal.valueOf(50.00), testWallet.getId(), "Гель для душа", Optional.of("О полмолив, мой нежный гель"));

    Transaction resultTrans = transactionService.createTransaction(transactionInfo);

    assertNotNull(resultTrans);
    assertEquals(100, walletRepository.findAll().getFirst().getBalance());
    assertEquals(testWallet.getId(), resultTrans.getWallet().getId());
  }

  @Test
  public void testTransactionCreationWalletNotFound() {
    var transactionInfo = new TransactionInfo(BigDecimal.valueOf(50.00), UUID.randomUUID(), "Гель для душа", Optional.of("О полмолив, мой нежный гель"));
    assertThrows(EntityNotFoundException.class, () -> transactionService.createTransaction(transactionInfo));
    assertEquals(100, walletRepository.findAll().getFirst().getBalance());
  }

  @Test
  public void testTransactionCreationDescriptionEmpty() {
    var transactionInfo = new TransactionInfo(BigDecimal.valueOf(50.00), testWallet.getId(), "Гель для душа", Optional.empty());
    assertDoesNotThrow(() -> transactionService.createTransaction(transactionInfo));
    assertEquals(100, walletRepository.findAll().getFirst().getBalance());
  }
}
