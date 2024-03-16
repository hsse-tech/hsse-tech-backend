package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(WalletService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WalletServiceTest extends DatabaseSuite {
  @Autowired
  private JpaUserRepository userRepository;

  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private WalletService walletService;

  private User testUser;

  @BeforeEach
  public void setUp() {
    passportRepository.deleteAll();
    userRepository.deleteAll();
    walletRepository.deleteAll();

    testUser = new User("test");
    HumanUserPassport testUserPassport = new HumanUserPassport(123L, "Test", "User", "test@phystech.edu");
    testUserPassport.setUser(testUser);

    passportRepository.save(testUserPassport);
    userRepository.save(testUser);
  }

  @Test
  public void testWalletCreation() {
    Wallet wallet = walletService.createWallet(testUser.getId());

    assertNotNull(wallet);
    assertEquals(testUser.getId(), wallet.getOwner().getId());
    assertTrue(walletRepository.existsById(wallet.getId()));
  }

  @Test
  public void testWalletCreationNoUserPassportFound() {
    assertThrows(WalletCreationException.class, () -> walletService.createWallet(UUID.randomUUID()));
  }
}
