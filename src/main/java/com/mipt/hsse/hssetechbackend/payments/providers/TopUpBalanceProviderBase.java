package com.mipt.hsse.hssetechbackend.payments.providers;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public interface TopUpBalanceProviderBase {
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  TopUpSession topUpBalance(UUID walletId, BigDecimal amount);
}
