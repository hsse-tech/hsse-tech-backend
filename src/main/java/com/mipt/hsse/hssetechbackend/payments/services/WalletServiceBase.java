package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Wallet;

import java.math.BigDecimal;
import java.util.UUID;

public interface WalletServiceBase {
  Wallet createWallet(UUID ownerId);

  Wallet getWallet(UUID id);

  Wallet changeWalletBalanceOn(UUID walletId, BigDecimal newBalance);
}
