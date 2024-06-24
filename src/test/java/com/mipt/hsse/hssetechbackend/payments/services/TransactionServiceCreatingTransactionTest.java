package com.mipt.hsse.hssetechbackend.payments.services;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.*;

@DataJpaTest
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
    passportRepository.deleteAll();
    userRepository.deleteAll();
    walletRepository.deleteAll();
  }

  @Test
  public void testTransactionCreation() {
    var transactionInfo = new TransactionInfo(BigDecimal.valueOf(50.00), testWallet.getId(), "Гель для душа", Optional.of("О полмолив, мой нежный гель"));

    Transaction resultTrans = transactionService.createTransaction(transactionInfo);

    assertNotNull(resultTrans);
    assertEquals(100, walletRepository.findAll().get(0).getBalance());
    assertEquals(testWallet.getId(), resultTrans.getWallet().getId());
  }

  @Test
  public void testTransactionCreationWalletNotFound() {
    var transactionInfo = new TransactionInfo(BigDecimal.valueOf(50.00), UUID.randomUUID(), "Гель для душа", Optional.of("О полмолив, мой нежный гель"));
    assertThrows(WalletNotFoundException.class, () -> transactionService.createTransaction(transactionInfo));
    assertEquals(100, walletRepository.findAll().get(0).getBalance());
  }

  @Test
  public void testTransactionCreationDescriptionEmpty() {
    var transactionInfo = new TransactionInfo(BigDecimal.valueOf(50.00), testWallet.getId(), "Гель для душа", Optional.empty());
    assertDoesNotThrow(() -> transactionService.createTransaction(transactionInfo));
    assertEquals(100, walletRepository.findAll().get(0).getBalance());
  }
}
