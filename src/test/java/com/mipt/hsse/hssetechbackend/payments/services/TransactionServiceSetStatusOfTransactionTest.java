package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TransactionService.class})
public class TransactionServiceSetStatusOfTransactionTest extends DatabaseSuite {
  @Autowired
  private JpaUserRepository userRepository;

  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private JpaTransactionRepository transactionRepository;

  @Autowired
  private TransactionService transactionService;

  private Transaction testTransaction;

  @BeforeEach
  public void setUp() {
    passportRepository.deleteAll();
    userRepository.deleteAll();
    walletRepository.deleteAll();

    var testUser = new User("test");
    var testUserPassport = new HumanUserPassport(123L, "Test", "User", "test@phystech.edu", testUser);
    var testWallet = new Wallet();
    testTransaction = new Transaction(BigDecimal.valueOf(50.00), "Test", "Some description");

    testWallet.setBalance(BigDecimal.valueOf(100.00));

    testWallet.setOwner(testUserPassport);
    testTransaction.setWallet(testWallet);
    testUserPassport.setUser(testUser);

    passportRepository.save(testUserPassport);
    userRepository.save(testUser);
    walletRepository.save(testWallet);
    transactionRepository.save(testTransaction);
  }

  @Test
  public void testSetTransactionSuccess() {
    Transaction transaction = transactionService.setTransactionStatus(testTransaction.getId(), ClientTransactionStatus.SUCCESS);

    assertNotNull(transaction);
    assertEquals(testTransaction.getId(), transaction.getId());
    assertEquals(ClientTransactionStatus.SUCCESS, transaction.getStatus());
  }

  @Test
  public void testSetTransactionFailed() {
    Transaction transaction = transactionService.setTransactionStatus(testTransaction.getId(), ClientTransactionStatus.FAILED);

    assertNotNull(transaction);
    assertEquals(testTransaction.getId(), transaction.getId());
    assertEquals(ClientTransactionStatus.FAILED, transaction.getStatus());
  }

  @Test
  public void testSetTransactionInProcess() {
    Transaction transaction = transactionService.setTransactionStatus(testTransaction.getId(), ClientTransactionStatus.IN_PROCESS);

    assertNotNull(transaction);
    assertEquals(testTransaction.getId(), transaction.getId());
    assertEquals(ClientTransactionStatus.IN_PROCESS, testTransaction.getStatus());
  }

  @Test
  public void testSetTransactionWhenItNotExists() {
    assertThrows(TransactionNotFoundException.class, () -> transactionService.setTransactionStatus(UUID.randomUUID(), ClientTransactionStatus.IN_PROCESS));
  }
}
