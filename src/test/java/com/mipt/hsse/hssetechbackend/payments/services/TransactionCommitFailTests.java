package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionManipulationException;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletUpdatingException;
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

import static com.mipt.hsse.hssetechbackend.BigDecimalHelper.assertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({TransactionService.class, WalletService.class})
public class TransactionCommitFailTests extends DatabaseSuite {
  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private TransactionService transactionService;

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
    passportRepository.deleteAll();
    userRepository.deleteAll();
    walletRepository.deleteAll();
  }

  @Test
  public void testCommitTransaction() throws TransactionManipulationException {
    var testTransaction = new Transaction(BigDecimal.valueOf(50), "Test", null);
    testTransaction.setWallet(testWallet);
    transactionRepository.save(testTransaction);

    transactionService.commitTransaction(testTransaction.getId());
    assertEquals(50, walletRepository.findAll().get(0).getBalance());
    assertEquals(ClientTransactionStatus.SUCCESS, transactionRepository.findAll().get(0).getStatus());
  }

  @Test
  public void testCommitNotEnoughMoney() {
    var testTransaction = new Transaction(BigDecimal.valueOf(300), "Test", null);
    testTransaction.setWallet(testWallet);
    transactionRepository.save(testTransaction);

    assertThrows(WalletUpdatingException.class,
            () -> transactionService.commitTransaction(testTransaction.getId()));
    assertEquals(ClientTransactionStatus.IN_PROCESS, transactionRepository.findAll().get(0).getStatus());
  }

  @Test
  public void testCommitAlreadyCommited() {
    var testTransaction = new Transaction(BigDecimal.valueOf(300), "Test", null);
    testTransaction.setWallet(testWallet);
    testTransaction.setStatus(ClientTransactionStatus.SUCCESS);
    transactionRepository.save(testTransaction);

    assertThrows(TransactionManipulationException.class,
            () -> transactionService.commitTransaction(testTransaction.getId()));
  }

  @Test
  public void testCommitAlreadyFailed() {
    var testTransaction = new Transaction(BigDecimal.valueOf(300), "Test", null);
    testTransaction.setWallet(testWallet);
    testTransaction.setStatus(ClientTransactionStatus.FAILED);
    transactionRepository.save(testTransaction);

    assertThrows(TransactionManipulationException.class,
            () -> transactionService.commitTransaction(testTransaction.getId()));
  }

  @Test
  public void testFailTransaction() throws TransactionManipulationException {
    var testTransaction = new Transaction(BigDecimal.valueOf(300), "Test", null);
    testTransaction.setWallet(testWallet);
    testTransaction.setStatus(ClientTransactionStatus.IN_PROCESS);
    transactionRepository.save(testTransaction);

    transactionService.failTransaction(testTransaction.getId());
    assertEquals(ClientTransactionStatus.FAILED, transactionRepository.findAll().get(0).getStatus());
  }

  @Test
  public void testFailWhenAlreadySuccess() {
    var testTransaction = new Transaction(BigDecimal.valueOf(300), "Test", null);
    testTransaction.setWallet(testWallet);
    testTransaction.setStatus(ClientTransactionStatus.SUCCESS);
    transactionRepository.save(testTransaction);

    assertThrows(TransactionManipulationException.class,
            () -> transactionService.failTransaction(testTransaction.getId()));
  }

  @Test
  public void testFailWhenAlreadyFailed() {
    var testTransaction = new Transaction(BigDecimal.valueOf(300), "Test", null);
    testTransaction.setWallet(testWallet);
    testTransaction.setStatus(ClientTransactionStatus.FAILED);
    transactionRepository.save(testTransaction);

    assertThrows(TransactionManipulationException.class,
            () -> transactionService.failTransaction(testTransaction.getId()));
  }
}
