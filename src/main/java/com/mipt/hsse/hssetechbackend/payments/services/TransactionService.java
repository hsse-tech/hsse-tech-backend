package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.TransactionNotFoundException;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService implements TransactionServiceBase {

  private final JpaWalletRepository jpaWalletRepository;
  private final JpaTransactionRepository jpaTransactionRepository;

  public TransactionService(JpaWalletRepository jpaWalletRepository,
                            JpaTransactionRepository jpaTransactionRepository) {
    this.jpaWalletRepository = jpaWalletRepository;
    this.jpaTransactionRepository = jpaTransactionRepository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Transaction createTransaction(TransactionInfo transactionInfo) {
    var walletOpt = jpaWalletRepository.findById(transactionInfo.walletId());

    if (walletOpt.isEmpty()) throw new WalletNotFoundException("Wallet not found to assign a new transaction");

    var wallet = walletOpt.get();
    var transaction = new Transaction(BigDecimal.valueOf(transactionInfo.amount()), transactionInfo.name(), transactionInfo.description().orElse(null));

    transaction.setWallet(wallet);
    jpaTransactionRepository.save(transaction);

    return transaction;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public Transaction setTransactionStatus(UUID id, ClientTransactionStatus status) {
    var targetTransactionOpt = jpaTransactionRepository.findById(id);

    if (targetTransactionOpt.isEmpty()) {
      throw new TransactionNotFoundException("Transaction for status update not found");
    }

    var targetTransaction = targetTransactionOpt.get();

    switch (status) {
      case SUCCESS -> targetTransaction.setIsSuccess(true);
      case IN_PROCESS -> targetTransaction.setIsSuccess(null);
      case FAILED -> targetTransaction.setIsSuccess(false);
    }

    jpaTransactionRepository.save(targetTransaction);

    return targetTransaction;
  }
}
