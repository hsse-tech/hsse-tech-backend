package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletUpdatingException;
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
@Import({WalletService.class})
public class WalletServiceSettingBalanceTest extends DatabaseSuite {
  @Autowired
  private JpaUserRepository userRepository;

  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private WalletService walletService;

  private Wallet testWallet;

  @BeforeEach
  public void setUp() {
    passportRepository.deleteAll();
    userRepository.deleteAll();
    walletRepository.deleteAll();

    var testUser = new User("test");
    var testUserPassport = new HumanUserPassport(123L, "Test", "User", "test@phystech.edu");
    testWallet = new Wallet();

    testWallet.setOwner(testUserPassport);
    testUserPassport.setUser(testUser);

    passportRepository.save(testUserPassport);
    userRepository.save(testUser);
    walletRepository.save(testWallet);
  }

  @Test
  public void testChangingBalance() {
    Wallet wallet = walletService.changeWalletBalanceOn(testWallet.getId(), BigDecimal.valueOf(100));

    assertNotNull(wallet);
    assertEquals(100, wallet.getBalance().doubleValue());

    wallet = walletService.changeWalletBalanceOn(testWallet.getId(), BigDecimal.valueOf(-100));

    assertNotNull(wallet);
    assertEquals(0, wallet.getBalance().doubleValue());

    assertThrows(WalletUpdatingException.class, () -> walletService.changeWalletBalanceOn(testWallet.getId(), BigDecimal.valueOf(-100)));
  }

  @Test
  public void testChangingBalanceWalletNotFound() {
    assertThrows(WalletNotFoundException.class, () -> walletService.changeWalletBalanceOn(UUID.randomUUID(), BigDecimal.ZERO));
  }
}
