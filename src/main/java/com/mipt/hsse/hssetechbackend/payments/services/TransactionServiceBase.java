package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface TransactionServiceBase {
  @Transactional(propagation = Propagation.REQUIRED)
  Transaction createTransaction(TransactionInfo transactionInfo);

  @Transactional(propagation = Propagation.REQUIRED)
  Transaction setTransactionStatus(UUID id, ClientTransactionStatus status);
}
