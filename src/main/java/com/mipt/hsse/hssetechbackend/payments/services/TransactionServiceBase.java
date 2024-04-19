package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionManipulationException;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;

import java.util.UUID;

public interface TransactionServiceBase {
  Transaction createTransaction(TransactionInfo transactionInfo);

  void commitTransaction(UUID id) throws TransactionManipulationException;

  void failTransaction(UUID id) throws TransactionManipulationException;
}
