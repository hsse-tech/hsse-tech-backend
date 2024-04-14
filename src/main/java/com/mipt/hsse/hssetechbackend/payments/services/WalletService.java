package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletCreationException;

import java.math.BigDecimal;
import java.util.UUID;

import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletUpdatingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService implements WalletServiceBase {
  private final JpaWalletRepository walletRepository;
  private final JpaHumanUserPassportRepository userRepository;

  public WalletService(JpaWalletRepository walletRepository,
                       JpaHumanUserPassportRepository userRepository) {
    this.walletRepository = walletRepository;
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Wallet createWallet(UUID ownerId) {
    var owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new WalletCreationException("Owner not found"));
    var wallet = new Wallet();

    wallet.setOwner(owner);
    wallet.setBalance(BigDecimal.ZERO);

    walletRepository.save(wallet);
    return wallet;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Wallet getWallet(UUID id) {
    return walletRepository.findById(id)
            .orElseThrow(WalletNotFoundException::new);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Wallet changeWalletBalanceOn(UUID walletId, BigDecimal delta) {
    var targetWallet = getWallet(walletId);

    var changedBalance = targetWallet.getBalance().add(delta);

    if (changedBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new WalletUpdatingException("Balance can't be less than zero");
    }

    targetWallet.setBalance(changedBalance);
    walletRepository.save(targetWallet);

    return targetWallet;
  }
}
