package com.mipt.hsse.hssetechbackend.payments.services;

import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaTransactionRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaWalletRepository;
import com.mipt.hsse.hssetechbackend.payments.exceptions.WalletNotFoundException;
import com.mipt.hsse.hssetechbackend.payments.services.dto.TransactionInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

  private final JpaWalletRepository jpaWalletRepository;
  private final JpaTransactionRepository jpaTransactionRepository;

  public TransactionService(JpaWalletRepository jpaWalletRepository,
                            JpaTransactionRepository jpaTransactionRepository) {
    this.jpaWalletRepository = jpaWalletRepository;
    this.jpaTransactionRepository = jpaTransactionRepository;
  }

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
}
