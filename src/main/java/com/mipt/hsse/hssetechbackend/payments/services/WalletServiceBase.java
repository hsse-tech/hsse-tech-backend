package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Wallet;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface WalletServiceBase {
  @Transactional(propagation = Propagation.REQUIRED)
  Wallet createWallet(UUID ownerId);

  @Transactional(propagation = Propagation.REQUIRED)
  Wallet getWallet(UUID id);
}
