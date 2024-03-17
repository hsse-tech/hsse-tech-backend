package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletCreationException;
import java.util.UUID;

import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {
  private final JpaWalletRepository walletRepository;
  private final JpaHumanUserPassportRepository userRepository;

  public WalletService(JpaWalletRepository walletRepository,
                       JpaHumanUserPassportRepository userRepository) {
    this.walletRepository = walletRepository;
    this.userRepository = userRepository;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Wallet createWallet(UUID ownerId) {
    var owner = userRepository.findById(ownerId);

    if (owner.isEmpty()) {
      throw new WalletCreationException("Owner not found");
    }
    var wallet = new Wallet();

    wallet.setOwner(owner.get());

    walletRepository.save(wallet);
    return wallet;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Wallet getWallet(UUID id) {
    var targetWalletOpt = walletRepository.findById(id);

    if (targetWalletOpt.isEmpty()) {
      throw new WalletNotFoundException("Wallet not found");
    }

    return targetWalletOpt.get();
  }
}
