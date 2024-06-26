package com.mipt.hsse.hssetechbackend.payments.services;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;

import java.math.BigDecimal;
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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(WalletService.class)
public class WalletServiceGettingWalletTest extends DatabaseSuite {
  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private WalletService walletService;

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
  public void testSimpleFinding() {
    Wallet wallet = walletService.getWallet(testWallet.getId());

    assertNotNull(wallet);
    assertEquals(testWallet.getId(), wallet.getId());
  }

  @Test
  public void testFindingNotExists() {
    assertThrows(WalletNotFoundException.class, () -> walletService.getWallet(UUID.randomUUID()));
  }
}
