package com.mipt.hsse.hssetechbackend.payments.providers;

import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionManipulationException;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionServiceBase;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.mipt.hsse.hssetechbackend.RublesToKopeckConverter.convertToKopeck;

@Service
public class TopUpBalanceProvider implements TopUpBalanceProviderBase {
  private final TransactionServiceBase transactionService;
  private final AcquiringSessionInitializer acquiringSessionInitializer;

  private static final String TOP_UP_TRANSACTION_NAME = "Пополнение баланса";

  public TopUpBalanceProvider(TransactionServiceBase transactionService,
                              AcquiringSessionInitializer acquiringSessionInitializer) {
    this.transactionService = transactionService;
    this.acquiringSessionInitializer = acquiringSessionInitializer;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public TopUpSession topUpBalance(UUID walletId, BigDecimal amount) {
    var transaction = transactionService.createTransaction(
            new TransactionInfo(amount.negate(), walletId, TOP_UP_TRANSACTION_NAME, Optional.empty()));

    var topUpSessionInfo = acquiringSessionInitializer.initialize(
            new SessionParams(convertToKopeck(amount), transaction.getId().toString()));

    try {
      if (!topUpSessionInfo.isSuccess()) {
        transactionService.failTransaction(transaction.getId());
      }
    } catch (TransactionManipulationException e) {
      throw new RuntimeException(e);
    }

    return new TopUpSession(topUpSessionInfo.isSuccess(), topUpSessionInfo.paymentUrl());
  }
}
