package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.ClientTransactionStatus;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;

import java.util.UUID;

public interface TransactionServiceBase {
  Transaction createTransaction(TransactionInfo transactionInfo);

  Transaction setTransactionStatus(UUID id, ClientTransactionStatus status);

  void commitTransaction(UUID id);

  void failTransaction(UUID id);
}
