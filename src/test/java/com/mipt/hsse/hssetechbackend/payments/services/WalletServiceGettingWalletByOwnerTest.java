package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.User;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaUserRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(WalletService.class)
public class WalletServiceGettingWalletByOwnerTest extends DatabaseSuite {
  @Autowired
  private JpaWalletRepository walletRepository;

  @Autowired
  private JpaHumanUserPassportRepository passportRepository;

  @Autowired
  private WalletService walletService;

  private Wallet testWallet;
  private HumanUserPassport testUserPassport;

  @BeforeEach
  public void setUp() {
    var testUser = new User("test");
    testUserPassport = new HumanUserPassport(123L, "Test", "User", "test@phystech.edu", testUser);
    testWallet = new Wallet();

    testWallet.setOwner(testUserPassport);
    testWallet.setBalance(BigDecimal.ZERO);
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
  public void testSimpleFinding() {
    Wallet wallet = walletService.getWalletByOwner(testUserPassport.getId());

    assertNotNull(wallet);
    assertEquals(testWallet.getId(), wallet.getId());
  }

  @Test
  public void testFindingNotExists() {
    assertThrows(EntityNotFoundException.class, () -> walletService.getWalletByOwner(UUID.randomUUID()));
  }
}
