package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.events;

import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionManipulationException;
import com.mipt.hsse.hssetechbackend.payments.providers.events.AcquiringEventsListener;
import com.mipt.hsse.hssetechbackend.payments.providers.events.MerchantNotification;
import com.mipt.hsse.hssetechbackend.payments.services.TransactionServiceBase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class TinkoffTopUpAcquiringEventsListener implements AcquiringEventsListener {
  private final TransactionServiceBase transactionService;

  private static final String CONFIRMED_TINKOFF_STATUS = "CONFIRMED";
  private static final String REVERSED_TINKOFF_STATUS = "REVERSED";
  private static final String REJECTED_TINKOFF_STATUS = "REJECTED";

  public TinkoffTopUpAcquiringEventsListener(TransactionServiceBase transactionService) {
    this.transactionService = transactionService;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void onAcquiringNotificationReceived(MerchantNotification notification) {
    var transactionId = UUID.fromString(notification.orderId());

    try {
      switch (notification.status()) {
        case CONFIRMED_TINKOFF_STATUS:
          transactionService.commitTransaction(transactionId);
          break;
        case REVERSED_TINKOFF_STATUS:
        case REJECTED_TINKOFF_STATUS:
          transactionService.failTransaction(transactionId);
          break;
      }
    } catch (TransactionManipulationException e) {
      throw new RuntimeException(e);
    }
  }
}
